package com.nazarukiv.csstudentshelperbot.service;


import com.nazarukiv.csstudentshelperbot.config.BotConfig;
import com.nazarukiv.csstudentshelperbot.model.User;
import com.nazarukiv.csstudentshelperbot.model.UserRepository;
import com.nazarukiv.csstudentshelperbot.service.resources.ResourceCategory;
import com.nazarukiv.csstudentshelperbot.service.resources.ResourceItem;
import com.nazarukiv.csstudentshelperbot.service.resources.ResourcesCatalog;


import com.vdurmont.emoji.EmojiParser;


import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    //static text
    private static final String HELP_TEXT =
            "Commands:\n" +
                    "/start - welcome\n" +
                    "/help - show help\n" +
                    "/resources - show resources for cs students";

    private static final String WELCOME_TEXT =
            "Hi! 👋\n" +
                    "I’m CS Students Helper Bot.\n\n" +
                    "Type /start.";

    @Autowired
    private UserRepository userRepository; //access to user table

    private final BotConfig config; //bot name, token from application.properties
    private final ResourcesCatalog catalog = new ResourcesCatalog();


    //constructor for bot
    public TelegramBot(BotConfig config) {
        this.config = config;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/help", "to get help with using bot"));
        listOfCommands.add(new BotCommand("/resources", "get resources for cs students"));

        try {
            execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's list: {}", e.getMessage());
        }
    }

    //getters for bot token and username
    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    //handle all incoming text and commands that bot receives
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            registerUser(message);

            String messageText = message.getText();
            long chatId = message.getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, message.getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "/resources":
                    sendResourcesMenu(chatId);
                    break;
                default:
                    sendMessage(chatId, "Incorrect command or text");
            }

            //handle keyboard clicks from user(callback queries)
        } else if (update.hasCallbackQuery()) {
            handleCallback(update);
        }
    }

    //saves user to database on first interaction only(and cheks does user already in database)
    private void registerUser(Message message) {
        Long chatId = message.getChatId();

        if (userRepository.findById(chatId).isEmpty()) {
            var chat = message.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUsername(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
        }
    }

    //handle /start command
    private void startCommandReceived(long chatId, String firstName) {
        String safeName = (firstName == null || firstName.isBlank()) ? "there" : firstName;
        String text = EmojiParser.parseToUnicode("Hello, " + safeName + "! :blush:\nType /help to see commands.");
        sendMessage(chatId, text);
    }

    //send keyboard to user to choose resource topic
    private void sendResourcesMenu(long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Choose a resources category:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (ResourceCategory category : ResourceCategory.values()) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(category.name());
            btn.setCallbackData("RES_CAT:" + category.name());

            rows.add(List.of(btn));
        }

        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: {}", e.getMessage());
        }

    }

    //handle button click from keyboard
    private void handleCallback(Update update) {
        String data = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        if (data != null && data.startsWith("RES_CAT:")) {
            String categoryName = data.substring("RES_CAT:".length());

            ResourceCategory category;
            try {
                category = ResourceCategory.valueOf(categoryName);
            } catch (IllegalArgumentException e) {
                sendMessage(chatId, "unknown category.");
                return;
            }

            sendResourcesForCategory(chatId, category);
        }
    }

    //send correct list of resources for specific category
    private void sendResourcesForCategory(long chatId, ResourceCategory category) {
        List<ResourceItem> items = catalog.get(category);

        if (items.isEmpty()) {
            sendMessage(chatId, "No resources yet for: " + category.name());
            return;
        }

        StringBuilder text = new StringBuilder();
        text.append(category.name()).append(" resources:\n\n");

        for (ResourceItem item : items) {
            text.append("• ").append(item.getTitle()).append("\n")
                    .append(item.getUrl()).append("\n");
            if (item.getNote() != null && !item.getNote().isBlank()) {
                text.append(item.getNote()).append("\n");
            }
            text.append("\n");
        }

        sendMessage(chatId, text.toString());
    }

    //method to send text messages
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: {}", e.getMessage());
        }
    }
}