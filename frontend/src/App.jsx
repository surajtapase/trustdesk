import { useEffect, useMemo, useState } from "react";
import axios from "axios";
import "./App.css";

const API_BASE = "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    "Content-Type": "application/json",
  },
});

function App() {
  const [tickets, setTickets] = useState([]);
  const [selectedTicketId, setSelectedTicketId] = useState(null);

  const [context, setContext] = useState(null);
  const [knowledge, setKnowledge] = useState([]);

  const [triage, setTriage] = useState(null);
  const [support, setSupport] = useState(null);

  const [traces, setTraces] = useState([]);
  const [action, setAction] = useState(null);

  const [loadingTickets, setLoadingTickets] = useState(true);
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [loadingAI, setLoadingAI] = useState(false);
  const [loadingAction, setLoadingAction] = useState(false);

  const [error, setError] = useState("");

  const selectedTicket = useMemo(
    () =>
      tickets.find((ticket) => ticket.ticketId === selectedTicketId) || null,
    [tickets, selectedTicketId]
  );

  const stats = useMemo(() => {
    const open = tickets.filter((t) => t.status === "open").length;

    const urgent = tickets.filter((t) => {
      const text = `${t.subject || ""} ${t.body || ""}`.toLowerCase();

      return (
        text.includes("battery") ||
        text.includes("security") ||
        text.includes("api key")
      );
    }).length;

    return {
      total: tickets.length,
      open,
      urgent,
      aiRuns: traces.length,
    };
  }, [tickets, traces]);

  useEffect(() => {
    loadTickets();
  }, []);

  useEffect(() => {
    if (selectedTicketId) {
      loadTicketDetails(selectedTicketId);
    }
  }, [selectedTicketId]);

  async function loadTickets() {
    try {
      setLoadingTickets(true);
      setError("");

      const response = await api.get("/tickets");

      setTickets(response.data);

      if (response.data.length > 0) {
        setSelectedTicketId((current) => current || response.data[0].ticketId);
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to connect to TrustDesk backend."
      );
    } finally {
      setLoadingTickets(false);
    }
  }

  async function loadTicketDetails(ticketId) {
    try {
      setLoadingDetails(true);
      setError("");

      setContext(null);
      setKnowledge([]);
      setTriage(null);
      setSupport(null);
      setTraces([]);
      setAction(null);

      const [contextResponse, knowledgeResponse, traceResponse] =
        await Promise.all([
          api.get(`/tickets/${ticketId}/context`),
          api.get(`/tickets/${ticketId}/knowledge`),
          api.get(`/traces/${ticketId}`),
        ]);

      setContext(contextResponse.data);
      setKnowledge(knowledgeResponse.data || []);
      setTraces(
        Array.isArray(traceResponse.data)
          ? traceResponse.data
          : traceResponse.data
            ? [traceResponse.data]
            : []
      );
    } catch (err) {
      setError(
        err.response?.data?.message ||
          `Unable to load details for ${ticketId}.`
      );
    } finally {
      setLoadingDetails(false);
    }
  }

  async function runTriage() {
    if (!selectedTicketId) return;

    try {
      setLoadingAI(true);
      setError("");

      const response = await api.post(
        `/ai/triage/${selectedTicketId}`
      );

      setTriage(response.data);
    } catch (err) {
      setError(
        err.response?.data?.message || "AI triage failed."
      );
    } finally {
      setLoadingAI(false);
    }
  }

  async function generateSupportResponse() {
    if (!selectedTicketId) return;

    try {
      setLoadingAI(true);
      setError("");

      const response = await api.post(
        `/ai/support/${selectedTicketId}`
      );

      setSupport(response.data);

      if (response.data?.triage) {
        setTriage(response.data.triage);
      }

      await refreshTraces(selectedTicketId);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to generate AI support response."
      );
    } finally {
      setLoadingAI(false);
    }
  }

  async function refreshTraces(ticketId = selectedTicketId) {
    if (!ticketId) return;

    try {
      const response = await api.get(`/traces/${ticketId}`);

      setTraces(
        Array.isArray(response.data)
          ? response.data
          : response.data
            ? [response.data]
            : []
      );
    } catch {
      // Trace refresh should not break the dashboard.
    }
  }

  async function requestRefundReview() {
    if (!selectedTicketId) return;

    try {
      setLoadingAction(true);
      setError("");

      const idempotencyKey =
        `trustdesk-${selectedTicketId}-${Date.now()}`;

      const response = await api.post(
        `/actions/refund-review/${selectedTicketId}`,
        null,
        {
          params: {
            requestedBy: "support-agent",
          },
          headers: {
            "Idempotency-Key": idempotencyKey,
          },
        }
      );

      setAction(response.data);
      await refreshTraces(selectedTicketId);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to create refund review action."
      );
    } finally {
      setLoadingAction(false);
    }
  }

  async function approveAction() {
    if (!action?.id) return;

    try {
      setLoadingAction(true);
      setError("");

      const response = await api.post(
        `/actions/${action.id}/approve`
      );

      setAction(response.data);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to approve action."
      );
    } finally {
      setLoadingAction(false);
    }
  }

  async function executeAction() {
    if (!action?.id) return;

    try {
      setLoadingAction(true);
      setError("");

      const response = await api.post(
        `/actions/${action.id}/execute`
      );

      setAction(response.data);
      await refreshTraces(selectedTicketId);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to execute action."
      );
    } finally {
      setLoadingAction(false);
    }
  }

  async function refreshDashboard() {
    await loadTickets();

    if (selectedTicketId) {
      await loadTicketDetails(selectedTicketId);
    }
  }

  function selectTicket(ticketId) {
    setSelectedTicketId(ticketId);
  }

  function formatDate(value) {
    if (!value) return "—";

    try {
      return new Date(value).toLocaleString();
    } catch {
      return value;
    }
  }

  function statusClass(value) {
    if (!value) return "";

    return value
      .toString()
      .toLowerCase()
      .replaceAll("_", "-")
      .replaceAll(" ", "-");
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">T</div>

          <div>
            <h1>TrustDesk</h1>
            <span>AI Support Operations</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          <button className="nav-item active">
            <span>▣</span>
            Dashboard
          </button>

          <button className="nav-item">
            <span>◉</span>
            Tickets
          </button>

          <button className="nav-item">
            <span>✦</span>
            AI Operations
          </button>

          <button className="nav-item">
            <span>▤</span>
            Audit Traces
          </button>
        </nav>

        <div className="sidebar-footer">
          <div className="system-status">
            <span className="status-dot"></span>
            Backend connected
          </div>

          <span className="backend-url">
            localhost:8080
          </span>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <p className="eyebrow">OPERATOR CONSOLE</p>
            <h2>Support Operations Dashboard</h2>
          </div>

          <button
            className="refresh-button"
            onClick={refreshDashboard}
            disabled={loadingTickets || loadingDetails}
          >
            ↻ Refresh
          </button>
        </header>

        {error && (
          <div className="error-banner">
            <strong>Something went wrong</strong>
            <span>{error}</span>

            <button onClick={() => setError("")}>×</button>
          </div>
        )}

        <section className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon">◉</div>
            <div>
              <span>Total Tickets</span>
              <strong>{stats.total}</strong>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">●</div>
            <div>
              <span>Open Tickets</span>
              <strong>{stats.open}</strong>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">!</div>
            <div>
              <span>Risk Signals</span>
              <strong>{stats.urgent}</strong>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">✦</div>
            <div>
              <span>AI Trace Events</span>
              <strong>{stats.aiRuns}</strong>
            </div>
          </div>
        </section>

        <section className="workspace">
          <div className="ticket-panel panel">
            <div className="panel-header">
              <div>
                <p className="section-label">INBOX</p>
                <h3>Tickets</h3>
              </div>

              <span className="count-badge">
                {tickets.length}
              </span>
            </div>

            {loadingTickets ? (
              <div className="loading-box">
                Loading tickets...
              </div>
            ) : tickets.length === 0 ? (
              <div className="empty-box">
                No tickets found.
              </div>
            ) : (
              <div className="ticket-list">
                {tickets.map((ticket) => (
                  <button
                    key={ticket.ticketId}
                    className={`ticket-item ${
                      selectedTicketId === ticket.ticketId
                        ? "selected"
                        : ""
                    }`}
                    onClick={() =>
                      selectTicket(ticket.ticketId)
                    }
                  >
                    <div className="ticket-item-top">
                      <span className="ticket-id">
                        {ticket.ticketId}
                      </span>

                      <span
                        className={`status-badge ${statusClass(
                          ticket.status
                        )}`}
                      >
                        {ticket.status}
                      </span>
                    </div>

                    <h4>{ticket.subject}</h4>

                    <p>
                      {(ticket.body || "").slice(0, 90)}
                      {ticket.body?.length > 90 ? "..." : ""}
                    </p>

                    <div className="ticket-meta">
                      <span>{ticket.channel}</span>
                      <span>
                        {formatDate(ticket.createdAt)}
                      </span>
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="details-area">
            {!selectedTicket ? (
              <div className="panel empty-state">
                <div className="empty-icon">◈</div>
                <h3>Select a ticket</h3>
                <p>
                  Choose a ticket from the inbox to inspect
                  context and run AI operations.
                </p>
              </div>
            ) : loadingDetails ? (
              <div className="panel loading-box large">
                Loading ticket context...
              </div>
            ) : (
              <>
                <section className="panel ticket-overview">
                  <div className="ticket-heading">
                    <div>
                      <div className="ticket-title-row">
                        <span className="ticket-id-large">
                          {selectedTicket.ticketId}
                        </span>

                        <span
                          className={`status-badge ${statusClass(
                            selectedTicket.status
                          )}`}
                        >
                          {selectedTicket.status}
                        </span>
                      </div>

                      <h2>{selectedTicket.subject}</h2>
                    </div>

                    <div className="ticket-date">
                      Created
                      <strong>
                        {formatDate(selectedTicket.createdAt)}
                      </strong>
                    </div>
                  </div>

                  <div className="ticket-body">
                    {selectedTicket.body}
                  </div>

                  <div className="ticket-info-grid">
                    <InfoItem
                      label="Customer"
                      value={selectedTicket.customerId}
                    />

                    <InfoItem
                      label="Order"
                      value={selectedTicket.orderId}
                    />

                    <InfoItem
                      label="Channel"
                      value={selectedTicket.channel}
                    />

                    <InfoItem
                      label="Status"
                      value={selectedTicket.status}
                    />
                  </div>
                </section>

                <section className="context-grid">
                  <div className="panel">
                    <SectionTitle
                      label="CUSTOMER"
                      title="Customer Context"
                    />

                    {context?.customer ? (
                      <div className="data-list">
                        {Object.entries(context.customer).map(
                          ([key, value]) => (
                            <DataRow
                              key={key}
                              label={key}
                              value={value}
                            />
                          )
                        )}
                      </div>
                    ) : (
                      <div className="empty-box">
                        No customer context.
                      </div>
                    )}
                  </div>

                  <div className="panel">
                    <SectionTitle
                      label="ORDER"
                      title="Order Context"
                    />

                    {context?.order ? (
                      <div className="data-list">
                        {Object.entries(context.order).map(
                          ([key, value]) => (
                            <DataRow
                              key={key}
                              label={key}
                              value={value}
                            />
                          )
                        )}
                      </div>
                    ) : (
                      <div className="empty-box">
                        No order context.
                      </div>
                    )}
                  </div>
                </section>

                <section className="panel ai-panel">
                  <div className="panel-header ai-header">
                    <div>
                      <p className="section-label">
                        ARTIFICIAL INTELLIGENCE
                      </p>
                      <h3>AI Operations</h3>
                    </div>

                    <div className="ai-actions">
                      <button
                        className="secondary-button"
                        onClick={runTriage}
                        disabled={loadingAI}
                      >
                        {loadingAI
                          ? "Running..."
                          : "Run Triage"}
                      </button>

                      <button
                        className="primary-button"
                        onClick={generateSupportResponse}
                        disabled={loadingAI}
                      >
                        {loadingAI
                          ? "Generating..."
                          : "Generate Support Response"}
                      </button>
                    </div>
                  </div>

                  {triage && (
                    <div className="triage-section">
                      <h4>AI Triage</h4>

                      <div className="triage-grid">
                        <Metric
                          label="Intent"
                          value={triage.intent}
                        />

                        <Metric
                          label="Priority"
                          value={triage.priority}
                          className={statusClass(
                            triage.priority
                          )}
                        />

                        <Metric
                          label="Escalation"
                          value={
                            triage.escalate
                              ? "Required"
                              : "Not required"
                          }
                          className={
                            triage.escalate
                              ? "urgent"
                              : "safe"
                          }
                        />
                      </div>

                      {triage.reason && (
                        <div className="reason-box">
                          <strong>Reason</strong>
                          <p>{triage.reason}</p>
                        </div>
                      )}
                    </div>
                  )}

                  {support && (
                    <div className="support-section">
                      <div className="support-heading">
                        <div>
                          <h4>Draft Support Response</h4>
                          <span>
                            AI-generated • Grounded in KB
                          </span>
                        </div>
                      </div>

                      <div className="draft-response">
                        {support.draftResponse}
                      </div>

                      <div className="citations">
                        <h4>Knowledge Citations</h4>

                        {support.citations?.length ? (
                          <div className="citation-list">
                            {support.citations.map(
                              (citation) => (
                                <span
                                  className="citation-chip"
                                  key={citation}
                                >
                                  {citation}
                                </span>
                              )
                            )}
                          </div>
                        ) : (
                          <span className="muted">
                            No citations returned.
                          </span>
                        )}
                      </div>
                    </div>
                  )}

                  {!triage && !support && (
                    <div className="ai-empty">
                      <div className="ai-empty-icon">✦</div>
                      <h4>No AI analysis yet</h4>
                      <p>
                        Run triage or generate a grounded
                        support response for this ticket.
                      </p>
                    </div>
                  )}
                </section>

                <section className="knowledge-trace-grid">
                  <div className="panel">
                    <SectionTitle
                      label="KNOWLEDGE BASE"
                      title="Retrieved Knowledge"
                    />

                    {knowledge.length ? (
                      <div className="knowledge-list">
                        {knowledge.map((doc, index) => (
                          <div
                            className="knowledge-card"
                            key={
                              doc.id ||
                              doc.documentId ||
                              index
                            }
                          >
                            <div className="knowledge-id">
                              {doc.documentId ||
                                doc.id ||
                                "KB"}
                            </div>

                            <strong>
                              {doc.title ||
                                doc.name ||
                                "Knowledge Document"}
                            </strong>

                            <p>
                              {doc.content ||
                                doc.text ||
                                "No document content available."}
                            </p>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="empty-box">
                        No knowledge documents returned.
                      </div>
                    )}
                  </div>

                  <div className="panel">
                    <SectionTitle
                      label="AUDIT"
                      title="AI Trace"
                    />

                    {traces.length ? (
                      <div className="trace-list">
                        {traces.map((trace, index) => (
                          <div
                            className="trace-card"
                            key={trace.id || index}
                          >
                            <div className="trace-top">
                              <strong>
                                {trace.runType}
                              </strong>

                              <span
                                className={`trace-status ${statusClass(
                                  trace.finalStatus
                                )}`}
                              >
                                {trace.finalStatus || "—"}
                              </span>
                            </div>

                            <div className="trace-row">
                              <span>Guardrail</span>
                              <strong>
                                {trace.guardrailResult ||
                                  "—"}
                              </strong>
                            </div>

                            <div className="trace-row">
                              <span>Documents</span>
                              <strong>
                                {trace.retrievedDocumentIds ||
                                  "—"}
                              </strong>
                            </div>

                            <div className="trace-row">
                              <span>Tool Actions</span>
                              <strong>
                                {trace.toolActions || "—"}
                              </strong>
                            </div>

                            <div className="trace-time">
                              {formatDate(trace.createdAt)}
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="empty-box">
                        No AI traces yet.
                      </div>
                    )}
                  </div>
                </section>

                <section className="panel action-panel">
                  <div className="panel-header">
                    <div>
                      <p className="section-label">
                        HUMAN APPROVAL
                      </p>
                      <h3>Refund Review Action</h3>
                    </div>

                    {action && (
                      <span
                        className={`action-status ${statusClass(
                          action.status
                        )}`}
                      >
                        {action.status}
                      </span>
                    )}
                  </div>

                  {!action ? (
                    <div className="action-start">
                      <div>
                        <strong>
                          Sensitive action requires approval
                        </strong>

                        <p>
                          Start a refund review only when the
                          ticket requires a refund decision.
                          The action will remain pending until
                          an operator approves it.
                        </p>
                      </div>

                      <button
                        className="primary-button"
                        onClick={requestRefundReview}
                        disabled={loadingAction}
                      >
                        {loadingAction
                          ? "Creating..."
                          : "Start Refund Review"}
                      </button>
                    </div>
                  ) : (
                    <div className="action-details">
                      <div className="action-info-grid">
                        <InfoItem
                          label="Action ID"
                          value={action.id}
                        />

                        <InfoItem
                          label="Action Type"
                          value={action.actionType}
                        />

                        <InfoItem
                          label="Ticket"
                          value={action.ticketId}
                        />

                        <InfoItem
                          label="Requested By"
                          value={action.requestedBy}
                        />

                        <InfoItem
                          label="Created"
                          value={formatDate(
                            action.createdAt
                          )}
                        />
                      </div>

                      <div className="action-buttons">
                        {action.status ===
                          "PENDING_APPROVAL" && (
                          <button
                            className="primary-button"
                            onClick={approveAction}
                            disabled={loadingAction}
                          >
                            {loadingAction
                              ? "Approving..."
                              : "Approve Action"}
                          </button>
                        )}

                        {action.status === "APPROVED" && (
                          <button
                            className="success-button"
                            onClick={executeAction}
                            disabled={loadingAction}
                          >
                            {loadingAction
                              ? "Executing..."
                              : "Execute Action"}
                          </button>
                        )}

                        {action.status === "EXECUTED" && (
                          <div className="completed-message">
                            ✓ Action executed successfully.
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </section>
              </>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}

function SectionTitle({ label, title }) {
  return (
    <div className="section-title">
      <p>{label}</p>
      <h3>{title}</h3>
    </div>
  );
}

function InfoItem({ label, value }) {
  return (
    <div className="info-item">
      <span>{label}</span>
      <strong>{value ?? "—"}</strong>
    </div>
  );
}

function DataRow({ label, value }) {
  return (
    <div className="data-row">
      <span>{formatLabel(label)}</span>
      <strong>
        {typeof value === "object"
          ? JSON.stringify(value)
          : value ?? "—"}
      </strong>
    </div>
  );
}

function Metric({ label, value, className = "" }) {
  return (
    <div className={`metric ${className}`}>
      <span>{label}</span>
      <strong>{value ?? "—"}</strong>
    </div>
  );
}

function formatLabel(value) {
  return value
    .replaceAll("_", " ")
    .replace(/([A-Z])/g, " $1")
    .replace(/^./, (char) => char.toUpperCase());
}

export default App;