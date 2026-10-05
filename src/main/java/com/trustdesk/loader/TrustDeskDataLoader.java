package com.trustdesk.loader;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.trustdesk.entity.Customer;
import com.trustdesk.entity.EvaluationCase;
import com.trustdesk.entity.KnowledgeDocument;
import com.trustdesk.entity.Order;
import com.trustdesk.entity.OrderItem;
import com.trustdesk.entity.Ticket;
import com.trustdesk.entity.ToolCatalog;
import com.trustdesk.repository.CustomerRepository;
import com.trustdesk.repository.EvaluationCaseRepository;
import com.trustdesk.repository.KnowledgeDocumentRepository;
import com.trustdesk.repository.OrderRepository;
import com.trustdesk.repository.TicketRepository;
import com.trustdesk.repository.ToolCatalogRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class TrustDeskDataLoader implements CommandLineRunner {

    private final ObjectMapper objectMapper;

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final ToolCatalogRepository toolCatalogRepository;
    private final EvaluationCaseRepository evaluationCaseRepository;

    private static final Path DATA_DIR = Path.of("data");

    public TrustDeskDataLoader(
            ObjectMapper objectMapper,
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            TicketRepository ticketRepository,
            KnowledgeDocumentRepository knowledgeDocumentRepository,
            ToolCatalogRepository toolCatalogRepository,
            EvaluationCaseRepository evaluationCaseRepository) {

        this.objectMapper = objectMapper;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.toolCatalogRepository = toolCatalogRepository;
        this.evaluationCaseRepository = evaluationCaseRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println("========================================");
        System.out.println("TrustDesk official data loading started");
        System.out.println("========================================");

        loadCustomers();
        loadOrders();
        loadTickets();
        loadToolCatalog();
        loadKnowledgeBase();
        loadEvaluationCases();

        System.out.println("========================================");
        System.out.println("TrustDesk official data loading completed");
        System.out.println("========================================");
    }

    private void loadCustomers() throws IOException {

        Path file = DATA_DIR.resolve("customers.json");

        if (!Files.exists(file)) {
            System.out.println("customers.json not found");
            return;
        }

        JsonNode root = objectMapper.readTree(file.toFile());

        if (!root.isArray()) {
            throw new IllegalStateException("customers.json must contain an array");
        }

        int loaded = 0;

        for (JsonNode node : root) {

            String customerId = text(node, "customer_id");

            if (customerId == null) {
                continue;
            }

            if (customerRepository.findByCustomerId(customerId).isPresent()) {
                continue;
            }

            Customer customer = new Customer();

            customer.setCustomerId(customerId);
            customer.setName(text(node, "name"));
            customer.setEmail(text(node, "email"));
            customer.setTier(text(node, "tier"));
            customer.setCountry(text(node, "country"));

            String createdAt = text(node, "created_at");

            if (createdAt != null) {
                customer.setCreatedAt(
                        LocalDate.parse(createdAt.substring(0, 10))
                );
            }

            customer.setVerified(
                    node.path("verified").asBoolean(false)
            );

            List<String> tags = new ArrayList<>();

            JsonNode tagsNode = node.get("tags");

            if (tagsNode != null && tagsNode.isArray()) {

                for (JsonNode tag : tagsNode) {
                    tags.add(tag.asText());
                }
            }

            customer.setTags(tags);

            customerRepository.save(customer);

            loaded++;
        }

        System.out.println("Customers loaded: " + loaded);
    }

    private void loadOrders() throws IOException {

        Path file = DATA_DIR.resolve("orders.json");

        if (!Files.exists(file)) {
            System.out.println("orders.json not found");
            return;
        }

        JsonNode root = objectMapper.readTree(file.toFile());

        if (!root.isArray()) {
            throw new IllegalStateException("orders.json must contain an array");
        }

        int loaded = 0;

        for (JsonNode node : root) {

            String orderId = text(node, "order_id");

            if (orderId == null) {
                continue;
            }

            if (orderRepository.findByOrderId(orderId).isPresent()) {
                continue;
            }

            Order order = new Order();

            order.setOrderId(orderId);
            order.setCustomerId(text(node, "customer_id"));
            order.setStatus(text(node, "status"));

            order.setPlacedAt(
                    parseDate(text(node, "placed_at"))
            );

            order.setDeliveredAt(
                    parseDate(text(node, "delivered_at"))
            );

            order.setEligibleReturnUntil(
                    parseDate(text(node, "eligible_return_until"))
            );

            String total = text(node, "total");

            if (total != null) {
                order.setTotal(new BigDecimal(total));
            }

            order.setCurrency(text(node, "currency"));
            order.setPaymentStatus(text(node, "payment_status"));
            order.setTrackingNumber(text(node, "tracking_number"));

            List<OrderItem> items = new ArrayList<>();

            JsonNode itemsNode = node.get("items");

            if (itemsNode != null && itemsNode.isArray()) {

                for (JsonNode itemNode : itemsNode) {

                    OrderItem item = new OrderItem();

                    item.setSku(text(itemNode, "sku"));
                    item.setName(text(itemNode, "name"));
                    item.setQuantity(
                            itemNode.path("quantity").asInt()
                    );
                    item.setCategory(
                            text(itemNode, "category")
                    );
                    item.setFinalSale(
                            itemNode.path("final_sale").asBoolean(false)
                    );

                    item.setOrder(order);

                    items.add(item);
                }
            }

            order.setItems(items);

            orderRepository.save(order);

            loaded++;
        }

        System.out.println("Orders loaded: " + loaded);
    }

    private void loadTickets() throws IOException {

        Path file = DATA_DIR.resolve("tickets.json");

        if (!Files.exists(file)) {
            System.out.println("tickets.json not found");
            return;
        }

        JsonNode root = objectMapper.readTree(file.toFile());

        if (!root.isArray()) {
            throw new IllegalStateException("tickets.json must contain an array");
        }

        int loaded = 0;

        for (JsonNode node : root) {

            String ticketId = text(node, "ticket_id");

            if (ticketId == null) {
                continue;
            }

            if (ticketRepository.findByTicketId(ticketId).isPresent()) {
                continue;
            }

            Ticket ticket = new Ticket();

            ticket.setTicketId(ticketId);
            ticket.setCustomerId(text(node, "customer_id"));
            ticket.setOrderId(text(node, "order_id"));
            ticket.setChannel(text(node, "channel"));
            ticket.setSubject(text(node, "subject"));
            ticket.setBody(text(node, "body"));

            String createdAt = text(node, "created_at");

            if (createdAt != null) {
                ticket.setCreatedAt(
                        OffsetDateTime.parse(createdAt)
                );
            }

            ticket.setStatus(text(node, "status"));

            ticketRepository.save(ticket);

            loaded++;
        }

        System.out.println("Tickets loaded: " + loaded);
    }

    private void loadToolCatalog() throws IOException {

        Path file = DATA_DIR.resolve("tool_actions.json");

        if (!Files.exists(file)) {
            System.out.println("tool_actions.json not found");
            return;
        }

        JsonNode root = objectMapper.readTree(file.toFile());

        if (!root.isArray()) {
            throw new IllegalStateException(
                    "tool_actions.json must contain an array"
            );
        }

        int loaded = 0;

        for (JsonNode node : root) {

            String toolName = extractToolName(node);

            if (toolName == null) {
                continue;
            }

            if (toolCatalogRepository
                    .findByToolName(toolName)
                    .isPresent()) {
                continue;
            }

            ToolCatalog tool = new ToolCatalog();

            tool.setToolName(toolName);
            tool.setDefinition(
                    objectMapper.writeValueAsString(node)
            );

            toolCatalogRepository.save(tool);

            loaded++;
        }

        System.out.println("Tool definitions loaded: " + loaded);
    }

    private void loadKnowledgeBase() throws IOException {

        Path directory = DATA_DIR.resolve("knowledge_base");

        if (!Files.exists(directory)) {
            System.out.println("knowledge_base directory not found");
            return;
        }

        int loaded = 0;

        try (var files = Files.list(directory)) {

            Iterator<Path> iterator =
                    files
                            .filter(path ->
                                    path.toString().endsWith(".md"))
                            .iterator();

            while (iterator.hasNext()) {

                Path file = iterator.next();

                String fileName = file.getFileName().toString();

                String content = Files.readString(file);

                String kbId = deriveKbId(fileName);

                if (knowledgeDocumentRepository
                        .findByKbId(kbId)
                        .isPresent()) {
                    continue;
                }

                KnowledgeDocument document =
                        new KnowledgeDocument();

                document.setKbId(kbId);
                document.setTitle(
                        createTitle(fileName)
                );
                document.setContent(content);
                document.setCategory(
                        deriveCategory(fileName)
                );

                knowledgeDocumentRepository.save(document);

                loaded++;
            }
        }

        System.out.println("Knowledge documents loaded: " + loaded);
    }

    private void loadEvaluationCases() throws Exception {

        Path path = Paths.get("data/eval_cases.jsonl");

        if (!Files.exists(path)) {
            return;
        }

        List<String> lines = Files.readAllLines(path);

        for (String line : lines) {

            if (line == null || line.isBlank()) {
                continue;
            }

            JsonNode node = objectMapper.readTree(line);

            String caseId = node.get("case_id").asText();

            EvaluationCase evaluationCase = new EvaluationCase();
            evaluationCase.setCaseId(caseId);
            evaluationCase.setCaseData(line);

            if (evaluationCaseRepository
                    .findByCaseId(caseId)
                    .isEmpty()) {

                evaluationCaseRepository.save(evaluationCase);
            }
        }

        System.out.println(
                "Evaluation cases loaded: " +
                        evaluationCaseRepository.count()
        );
    }

    private String text(JsonNode node, String field) {

        JsonNode value = node.get(field);

        if (value == null ||
                value.isNull()) {
            return null;
        }

        return value.asText();
    }

    private LocalDate parseDate(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(
                value.substring(0, 10)
        );
    }

    private String extractToolName(JsonNode node) {

        String[] fields = {
                "tool_name",
                "action_type",
                "action",
                "name"
        };

        for (String field : fields) {

            String value = text(node, field);

            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private String extractCaseId(JsonNode node) {

        String[] fields = {
                "case_id",
                "id",
                "eval_id"
        };

        for (String field : fields) {

            String value = text(node, field);

            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private String deriveKbId(String fileName) {

        String name =
                fileName
                        .replace(".md", "")
                        .toLowerCase();

        if (name.contains("refund")) {
            return "KB-REFUND-001";
        }

        if (name.contains("shipping")) {
            return "KB-SHIPPING-001";
        }

        if (name.contains("warranty")) {
            return "KB-WARRANTY-001";
        }

        if (name.contains("account_security")) {
            return "KB-ACCOUNT-001";
        }

        if (name.contains("billing")) {
            return "KB-BILLING-001";
        }

        if (name.contains("coupon")) {
            return "KB-COUPON-001";
        }

        if (name.contains("support_security")) {
            return "KB-SECURITY-001";
        }

        if (name.contains("adversarial")) {
            return "KB-ADVERSARIAL-001";
        }

        return "KB-" + name.toUpperCase();
    }

    private String deriveCategory(String fileName) {

        String name =
                fileName
                        .replace(".md", "")
                        .toLowerCase();

        if (name.contains("refund")) {
            return "refund";
        }

        if (name.contains("shipping")) {
            return "shipping";
        }

        if (name.contains("warranty")) {
            return "warranty";
        }

        if (name.contains("account_security")) {
            return "account_security";
        }

        if (name.contains("billing")) {
            return "billing";
        }

        if (name.contains("coupon")) {
            return "coupon";
        }

        if (name.contains("support_security")) {
            return "security";
        }

        if (name.contains("adversarial")) {
            return "adversarial";
        }

        return "general";
    }

    private String createTitle(String fileName) {

        String title =
                fileName
                        .replace(".md", "")
                        .replace("_", " ");

        return title.substring(0, 1).toUpperCase()
                + title.substring(1);
    }
}