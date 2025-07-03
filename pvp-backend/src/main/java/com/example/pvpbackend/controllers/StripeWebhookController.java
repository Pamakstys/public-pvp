package com.example.pvpbackend.controllers;

import com.example.pvpbackend.services.BillService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
public class StripeWebhookController {
    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final BillService billService;
    public StripeWebhookController(BillService billService){
        this.billService = billService;
    }

    @PostMapping
    public Object handleStripeEvent(HttpServletRequest request) throws Exception {
        new Thread(() -> {
            try {
                handleStripeEventInner(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
        return ResponseEntity.ok().build();
    }

    public void handleStripeEventInner(HttpServletRequest request) throws Exception{
        Event event;
        Session session = null;
        Boolean success = false;
        try {
            String sigHeader = request.getHeader("Stripe-Signature");

            ServletInputStream inputStream = request.getInputStream();
            byte[] payloadBytes = inputStream.readAllBytes();
            String payload = new String(payloadBytes, StandardCharsets.UTF_8);

            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            if(deserializer.getObject().isPresent()) {
                StripeObject stripeObject = deserializer.getObject().get();

                if(stripeObject instanceof Session parsedSession){
                    session = parsedSession;

                    if("checkout.session.completed".equals(event.getType())){
                        Map<String, Object> params = new HashMap<>();
                        params.put("expand", List.of("line_items"));

                        Session fullSession = Session.retrieve(session.getId(), params, null);

                        List<LineItem> lineItems = fullSession.getLineItems().getData();
                        success = billService.payBills(lineItems);

                        if(success == null || !success) {
                            issueRefund(session);
                            throw new Exception("Payment failed and refund issued.");
                        }
                    }
                }
            }
            if (session != null && !success) {
                throw new Exception("Payment failed and refund issued.");
            }
        } catch (Exception e) {
            if (session != null) {
                try {
                    issueRefund(session);
                } catch (StripeException refundEx) {

                }
            }
        }
    }

    public void issueRefund(Session session) throws StripeException {
        String paymentIntentId = session.getPaymentIntent();
        if(paymentIntentId != null) {
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();
            Refund refund = Refund.create(refundParams);
        }
    }
}
