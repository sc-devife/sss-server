package com.sss.app.service.quotationtemplate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.sss.app.service.quotationtemplate.QuotationDataMapUtil.map;

/**
 * Predefined, realistic-looking quotation data used ONLY by the Settings
 * template preview — lets an admin check a template's design/structure
 * without needing a real Escape. Shape must match QuotationDataService's
 * real-data map exactly, since both are fed to the same
 * QuotationRenderingService.
 */
@Service
public class SampleQuotationDataService {

    // Reuses the existing app.frontend-url property (already used elsewhere
    // for things like invite links) rather than adding a duplicate logo file
    // to the backend — the frontend's public/logo.jpg is served at this
    // origin regardless of who ends up fetching the rendered HTML (a
    // browser, or later a server-side PDF renderer).
    @Value("${app.frontend-url}")
    private String frontendUrl;

    public Map<String, Object> buildSampleData() {
        return map(
                "organization", map(
                        "name", "Wanderlust Escapes Pvt Ltd",
                        "logoUrl", frontendUrl + "/logo.jpg",
                        "tagline", "Curated journeys, unforgettable memories",
                        "contactEmail", "hello@wanderlustescapes.example",
                        "contactPhone", "+91 98765 43210",
                        "websiteUrl", "www.wanderlustescapes.example",
                        "gstin", "27AAAAA0000A1Z5",
                        "address", map(
                                "streetFirst", "42 Marine Drive", "streetSecond", "Bandra West", "landMark", "Near Bandstand",
                                "city", "Mumbai", "state", "Maharashtra", "country", "India", "zipCode", "400050",
                                "contactNumber", "+91 98765 43210", "contactEmail", "hello@wanderlustescapes.example"
                        )
                ),
                "lead", map(
                        "name", "Rahul & Priya Sharma",
                        "email", "rahul.sharma@example.com",
                        "phone", "+91 90000 11122"
                ),
                "travellers", List.of(
                        map("name", "Rahul Sharma", "type", "ADULT", "email", "rahul.sharma@example.com", "phone", "+91 90000 11122", "isPrimary", true),
                        map("name", "Priya Sharma", "type", "ADULT", "email", "", "phone", "", "isPrimary", false)
                ),
                "escapePoints", List.of(
                        map("name", "Bali", "locationLabel", "Kuta, Bali, Indonesia",
                                "description", "Beaches, temples, and vibrant nightlife.", "imageUrl", ""),
                        map("name", "Lakshadweep", "locationLabel", "Kavaratti, Lakshadweep, India",
                                "description", "Pristine lagoons and coral reefs.", "imageUrl", "")
                ),
                "startDate", "2026-11-10",
                "endDate", "2026-11-16",
                "numberOfDays", 6,
                "days", List.of(
                        map("dayNumber", 1, "items", List.of(
                                map("itemType", "transport", "title", "Airport pickup", "startTime", "10:00",
                                        "notes", "Private cab from Ngurah Rai Airport to hotel", "price", "2500"),
                                map("itemType", "hotel", "title", "Check-in — The Ocean Resort", "startTime", "14:00",
                                        "notes", null, "price", null,
                                        "hotel", map("roomCount", 1, "paxPerRoom", 2, "totalPrice", "8500",
                                                "mealPlanName", "CP — Continental Plan", "roomTypeName", "Deluxe Room"))
                        )),
                        map("dayNumber", 2, "items", List.of(
                                map("itemType", "activity", "title", "Scuba diving experience", "startTime", "09:00",
                                        "notes", "Includes equipment and instructor", "price", "3500")
                        )),
                        map("dayNumber", 3, "items", List.of(
                                map("itemType", "sightseeing", "title", "Uluwatu Temple & Kecak Dance", "startTime", "16:00",
                                        "notes", "Sunset viewing included", "price", "1500"),
                                map("itemType", "transport", "title", "Onward flight", "startTime", "07:30",
                                        "notes", null, "price", null,
                                        "transport", map(
                                                "modeCode", "flight", "vehicleTypeCode", "economy", "tripType", "one_way", "sellingPrice", "8500",
                                                "legs", List.of(map(
                                                        "direction", "onward",
                                                        "departureAirport", "DPS - Ngurah Rai", "departureTerminal", "T1", "departureTime", "2026-11-13T07:30:00",
                                                        "arrivalAirport", "AGX - Agatti", "arrivalTerminal", null, "arrivalTime", "2026-11-13T10:15:00",
                                                        "flightNumber", "6E-2145"
                                                ))
                                        ))
                        ))
                ),
                "pricing", map(
                        "currencyCode", "INR",
                        "subtotal", "95000.00",
                        "tax", "4750.00",
                        "discount", "5000.00",
                        "total", "94750.00"
                ),
                "inclusions", List.of(
                        map("name", "Accommodation", "contentHtml", "<p>5 nights in a Deluxe Room with daily breakfast.</p>"),
                        map("name", "Transfers", "contentHtml", "<p>All airport and sightseeing transfers by private vehicle.</p>")
                ),
                "exclusions", List.of(
                        map("name", "Flights", "contentHtml", "<p>International and domestic airfare not included.</p>"),
                        map("name", "Personal expenses", "contentHtml", "<p>Tips, laundry, and other personal expenses.</p>")
                ),
                "terms", List.of(
                        map("name", "Cancellation policy", "contentHtml", "<p>Full refund if cancelled 30 days before departure.</p>")
                ),
                "payment", map(
                        "milestones", List.of(
                                map("label", "Booking advance", "dueDate", "2026-10-01", "amount", "30000.00", "status", "pending"),
                                map("label", "Balance payment", "dueDate", "2026-11-01", "amount", "64750.00", "status", "pending")
                        )
                )
        );
    }
}
