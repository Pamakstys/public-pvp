package com.example.pvpbackend.services;

import com.example.pvpbackend.models.Bill;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StripeService {
    public String createCheckoutSession(List<Bill> bills) throws Exception {
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        for (Bill bill : bills) {
            SessionCreateParams.LineItem item = SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("eur")
                                    .setUnitAmount((long) (bill.getSelectedAmountToPay() * 100)) // Stripe naudoja centus, delto x 100
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(bill.getImones_pavadinimas())
                                                    .setDescription(bill.getAprasymas())
                                                    .putMetadata("billId", bill.getId().toString())
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            lineItems.add(item);
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3000/payment/success")
                .setCancelUrl("http://localhost:3000/payment/cancel")
                .addAllLineItem(lineItems)
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}
