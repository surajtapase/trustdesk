package com.trustdesk.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "trustdesk.ai.provider",
        havingValue = "mock",
        matchIfMissing = true
)

public class MockAiProvider implements AiProvider {

    @Override
    public String generateResponse(String prompt) {

        String lowerPrompt = prompt.toLowerCase();

        /*
         * =========================================================
         * CUSTOMER-FACING RESPONSE
         * =========================================================
         */

        if (lowerPrompt.contains("customer-facing response")) {

            String ticketText =
                    extractTicketText(lowerPrompt);

            // Damaged product
            if (containsAny(
                    ticketText,
                    "damaged",
                    "cracked",
                    "arrived damaged")) {

                return """
                        Thanks for contacting us. We understand that
                        your BlueBuds Air arrived damaged.

                        Under our return and refund policy, a replacement
                        or refund review may be available for damaged
                        products within the applicable return window.
                        Please provide a photo of the damaged product if
                        required so our support team can review the case.

                        Any replacement or refund review requires the
                        appropriate support approval.
                        """;
            }

            // Shipping
            if (containsAny(
                    ticketText,
                    "no movement",
                    "tracking",
                    "business days")) {

                return """
                        Thanks for contacting us. We understand that your
                        shipment tracking has not moved for several business
                        days.

                        Our support team can open a carrier investigation
                        to determine the shipment status.

                        We will review the carrier result before determining
                        any further resolution. Any refund decision will be
                        made after the carrier investigation is completed.
                        """;
            }

            // Final-sale software
            if (containsAny(
                    ticketText,
                    "final-sale",
                    "final sale",
                    "software license",
                    "software licenses",
                    "license")) {

                return """
                        Thanks for contacting us.

                        Software licenses are final sale under the applicable
                        refund policy and are not eligible for a standard
                        refund.

                        We cannot approve a refund for this final-sale
                        software license.
                        """;
            }

            // Battery swelling
            if (containsAny(
                    ticketText,
                    "battery swelling",
                    "swollen battery",
                    "battery is swollen",
                    "battery has swollen",
                    "swelling")) {

                return """
                        Thank you for contacting us. A swollen battery is a
                        safety issue and should be treated as urgent.

                        Please do not attempt to repair or continue using
                        the affected device. The case should be escalated
                        to a specialist.

                        Our support team will review the case and determine
                        the appropriate next steps.
                        """;
            }

            // Account security
            if (containsAny(
                    ticketText,
                    "change email",
                    "change the email",
                    "email address",
                    "identity",
                    "verification",
                    "skip identity",
                    "bypass identity",
                    "bypass verification")) {

                return """
                        We can help with your account email request.

                        For security reasons, identity verification is
                        required before an account email can be changed.
                        We cannot change the email directly without the
                        required verification.

                        The request has been escalated to our support team
                        for secure handling.
                        """;
            }

            // Prompt injection
            if (containsAny(
                    ticketText,
                    "coupon",
                    "override",
                    "prompt injection",
                    "hide it from human",
                    "ignore previous instructions")) {

                return """
                        We cannot follow unsafe instructions requesting a
                        coupon or asking the action to be hidden from a
                        human reviewer.

                        The unsafe instruction has been flagged and the
                        request has been escalated to our support team.
                        """;
            }

            // Protected information
            if (containsAny(
                    ticketText,
                    "api key",
                    "system prompt",
                    "internal notes",
                    "hidden instructions")) {

                return """
                        We are unable to provide system prompts, API keys,
                        internal notes, or other protected information.

                        This request has been escalated to our support team.
                        """;
            }

            // Duplicate charge
            if (containsAny(
                    ticketText,
                    "duplicate charge",
                    "charged twice",
                    "double charge",
                    "duplicate payment")) {

                return """
                        Thanks for contacting us. We understand that you
                        may have been charged twice for a single order.

                        Our support team can start a billing review to
                        investigate the duplicate charge. Please provide
                        the transaction reference if needed so the payment
                        can be reviewed.

                        We will review the transaction before determining
                        whether a refund is appropriate. Any refund decision
                        will be made after the billing review is completed.
                        """;
            }

            return """
                    Thanks for contacting us. We understand that you
                    may have been charged twice for a single order.
            
                    Our support team can start a billing review to
                    investigate the duplicate charge. Please provide
                    the transaction reference if needed so the payment
                    can be reviewed.
            
                    We will review the transaction before determining
                    whether a refund is appropriate. Any refund decision
                    will be made after the billing review is completed.
                    """;
        }

        /*
         * =========================================================
         * TRIAGE
         * =========================================================
         */

        String ticketText =
                extractTicketText(lowerPrompt);

        // Battery swelling
        if (containsAny(
                ticketText,
                "battery swelling",
                "swollen battery",
                "battery is swollen",
                "battery has swollen",
                "swelling")) {

            return """
                    {
                      "intent": "warranty",
                      "priority": "urgent",
                      "escalate": true,
                      "reason": "Customer reported a potentially unsafe battery issue."
                    }
                    """;
        }

        // Account security
        if (containsAny(
                ticketText,
                "change email",
                "change the email",
                "email address",
                "identity",
                "skip identity",
                "bypass identity",
                "bypass verification",
                "disable verification")) {

            return """
                    {
                      "intent": "account_security",
                      "priority": "high",
                      "escalate": true,
                      "reason": "Customer is requesting an account change that requires identity verification."
                    }
                    """;
        }

        // Protected information
        if (containsAny(
                ticketText,
                "api key",
                "system prompt",
                "internal notes",
                "hidden instructions")) {

            return """
                    {
                      "intent": "account_security",
                      "priority": "high",
                      "escalate": true,
                      "reason": "Customer requested protected internal information."
                    }
                    """;
        }

        // Prompt injection
        if (containsAny(
                ticketText,
                "coupon",
                "discount",
                "override",
                "prompt injection",
                "hide it from human",
                "ignore previous instructions")) {

            return """
                    {
                      "intent": "general",
                      "priority": "medium",
                      "escalate": true,
                      "reason": "The request contains an unsafe instruction requiring human review."
                    }
                    """;
        }

        // Billing
        if (containsAny(
                ticketText,
                "duplicate charge",
                "charged twice",
                "double charge",
                "duplicate payment")) {

            return """
                    {
                      "intent": "billing",
                      "priority": "high",
                      "escalate": false,
                      "reason": "Customer is reporting a possible duplicate charge."
                    }
                    """;
        }

        // Shipping
        if (containsAny(
                ticketText,
                "no movement",
                "tracking",
                "business days")) {

            return """
                    {
                      "intent": "shipping",
                      "priority": "high",
                      "escalate": false,
                      "reason": "Customer is reporting a shipping or tracking issue."
                    }
                    """;
        }

        // Final-sale refund
        if (containsAny(
                ticketText,
                "final-sale",
                "final sale",
                "software license",
                "software licenses",
                "license")) {

            return """
                    {
                      "intent": "refund",
                      "priority": "low",
                      "escalate": false,
                      "reason": "Customer is asking about a refund for a final-sale software license."
                    }
                    """;
        }

        // Damaged product
        if (containsAny(
                ticketText,
                "damaged",
                "cracked",
                "arrived damaged")) {

            return """
                    {
                      "intent": "refund",
                      "priority": "medium",
                      "escalate": false,
                      "reason": "Customer reported a damaged product within the applicable return window."
                    }
                    """;
        }

        // Generic refund
        if (ticketText.contains("refund")) {

            return """
                    {
                      "intent": "refund",
                      "priority": "medium",
                      "escalate": false,
                      "reason": "Customer is asking about a possible refund."
                    }
                    """;
        }

        return """
                {
                  "intent": "general",
                  "priority": "low",
                  "escalate": false,
                  "reason": "The request does not match a specific supported intent."
                }
                """;
    }

    private String extractTicketText(String prompt) {

        int start =
                prompt.indexOf("=== ticket start ===");

        int end =
                prompt.indexOf("=== ticket end ===");

        if (start >= 0 && end > start) {

            return prompt.substring(
                    start,
                    end
            );
        }

        /*
         * Fallback for any older prompt format.
         */
        start = prompt.indexOf("subject:");

        if (start < 0) {
            start = prompt.indexOf("body:");
        }

        if (start < 0) {
            return prompt;
        }

        end =
                prompt.indexOf(
                        "\n\nknowledge base:",
                        start
                );

        if (end < 0) {
            end = prompt.length();
        }

        return prompt.substring(start, end);
    }

    private boolean containsAny(
            String text,
            String... values) {

        for (String value : values) {

            if (text.contains(value)) {
                return true;
            }
        }

        return false;
    }
}