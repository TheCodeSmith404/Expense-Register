package com.tcssol.expensetracker.Utils;

import com.tcssol.expensetracker.Model.Observation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses bank / UPI / wallet transaction SMS into an {@link Observation}.
 *
 * A message is accepted only when ALL of these hold:
 *   1. it contains an amount ("Rs 500", "INR 1,250.00", "₹99"),
 *   2. it contains a debit or credit keyword,
 *   3. it mentions a financial context (account/UPI/card/bank/wallet),
 *   4. it does not look like an OTP, promo, request or failed transaction.
 */
public final class BankSmsParser {

    private static final Pattern AMOUNT = Pattern.compile(
            "(?:rs\\.?|inr|₹)\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern DEBIT = Pattern.compile(
            "\\b(debited|debit|spent|paid|sent|withdrawn|deducted|purchase(?:d)?)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CREDIT = Pattern.compile(
            "\\b(credited|received|deposited|refund(?:ed)?)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CONTEXT = Pattern.compile(
            "\\b(a/c|ac|acct|account|upi|card|bank|wallet|vpa|imps|neft|rtgs|atm)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern IGNORE = Pattern.compile(
            "\\b(otp|one\\s*time\\s*password|verification\\s*code|has\\s*requested|payment\\s*request|" +
                    "will\\s*be\\s*debited|declined|failed|unsuccessful|reversed|emi\\s*due|offer|cashback\\s*up\\s*to|win|loan\\s*approved)\\b",
            Pattern.CASE_INSENSITIVE);

    private BankSmsParser() {
    }

    /** @return a parsed observation, or null when the message is not a transaction. */
    public static Observation parse(String sender, String body, long timeMillis) {
        if (body == null || body.isEmpty()) return null;
        if (IGNORE.matcher(body).find()) return null;
        if (!CONTEXT.matcher(body).find()) return null;

        Matcher amountMatcher = AMOUNT.matcher(body);
        if (!amountMatcher.find()) return null;
        double amount;
        try {
            amount = Double.parseDouble(amountMatcher.group(1).replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
        if (amount <= 0) return null;

        Matcher debit = DEBIT.matcher(body);
        Matcher credit = CREDIT.matcher(body);
        boolean hasDebit = debit.find();
        boolean hasCredit = credit.find();
        if (!hasDebit && !hasCredit) return null;

        boolean isCredit;
        if (hasDebit && hasCredit) {
            // Both present ("credited to X, debited from your a/c") — earliest keyword wins
            isCredit = credit.start() < debit.start();
        } else {
            isCredit = hasCredit;
        }

        String trimmedBody = body.length() > 300 ? body.substring(0, 300) : body;
        return new Observation(amount, isCredit, timeMillis,
                sender == null ? "" : sender, trimmedBody);
    }
}
