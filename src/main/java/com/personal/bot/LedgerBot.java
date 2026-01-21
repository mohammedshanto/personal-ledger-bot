package com.personal.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class LedgerBot extends TelegramLongPollingBot {

    // ✅ YOUR TELEGRAM USER ID (ONLY CHANGE HERE)
    private static final long OWNER_ID = 1749194456L;

    private final BotData data = DataStore.load();

    @Override
    public String getBotUsername() {
        return "YOUR_BOT_USERNAME"; // 🔴 BotFather username here (no @)
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {

        // ===== HANDLE RESET CONFIRM BUTTON =====
        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals("RESET_CONFIRM")) {
                data.bkash = new Ledger();
                data.nagad = new Ledger();
                DataStore.save(data);

                send(
                    update.getCallbackQuery().getMessage().getChatId(),
                    "✅ Reset completed.\nNew month started."
                );
            }
            return;
        }

        if (!update.hasMessage()) return;

        // 🔐 PRIVATE BOT CHECK (ONLY YOU CAN USE)
        if (update.getMessage().getFrom().getId() != OWNER_ID) return;

        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (text.startsWith("/bkashin")) {
            handle(chatId, data.bkash, text, 3.75, true);
        } 
        else if (text.startsWith("/bkashout")) {
            handle(chatId, data.bkash, text, 4.0, false);
        } 
        else if (text.startsWith("/nagadin")) {
            handle(chatId, data.nagad, text, 3.90, true);
        } 
        else if (text.startsWith("/nagadout")) {
            handle(chatId, data.nagad, text, 4.10, false);
        } 
        else if (text.equals("/summary")) {
            send(chatId, summary());
        } 
        else if (text.equals("/reset")) {
            sendResetConfirm(chatId);
        }
    }

    private void handle(long chatId, Ledger ledger, String text, double rate, boolean isIn) {
        double amount = Double.parseDouble(text.split(" ")[1]);
        double profit = (amount / 1000) * rate;

        if (isIn) ledger.cashIn += amount;
        else ledger.cashOut += amount;

        ledger.profit += profit;
        DataStore.save(data);

        send(
            chatId,
            "Amount: " + amount +
            "\nProfit: " + String.format("%.2f", profit)
        );
    }

    private String summary() {
        return "📊 Summary\n\n" +
                "bKash Profit: " + String.format("%.2f", data.bkash.profit) + "\n" +
                "Nagad Profit: " + String.format("%.2f", data.nagad.profit);
    }

    private void sendResetConfirm(long chatId) {
        InlineKeyboardButton yes = new InlineKeyboardButton("✅ Confirm Reset");
        yes.setCallbackData("RESET_CONFIRM");

        InlineKeyboardMarkup markup =
                new InlineKeyboardMarkup(List.of(List.of(yes)));

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText("⚠️ Monthly reset confirm?");
        msg.setReplyMarkup(markup);

        try {
            execute(msg);
        } catch (Exception ignored) {}
    }

    private void send(long chatId, String text) {
        try {
            execute(new SendMessage(String.valueOf(chatId), text));
        } catch (Exception ignored) {}
    }
}
