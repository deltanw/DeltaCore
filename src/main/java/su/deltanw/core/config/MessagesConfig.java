package su.deltanw.core.config;

import net.elytrium.commons.config.YamlConfig;
import net.elytrium.commons.kyori.serialization.Serializers;

public class MessagesConfig extends YamlConfig {

  @Ignore
  public static final MessagesConfig INSTANCE = new MessagesConfig();

  @Comment({
      "Available serializers:",
      "LEGACY_AMPERSAND - \"&c&lExample &c&9Text\".",
      "LEGACY_SECTION - \"§c§lExample §c§9Text\".",
      "MINIMESSAGE - \"<bold><red>Example</red> <blue>Text</blue></bold>\". (https://webui.adventure.kyori.net/)",
      "GSON - \"[{\"text\":\"Example\",\"bold\":true,\"color\":\"red\"},{\"text\":\" \",\"bold\":true},{\"text\":\"Text\",\"bold\":true,\"color\":\"blue\"}]\". (https://minecraft.tools/en/json_text.php/)",
      "GSON_COLOR_DOWNSAMPLING - Same as GSON, but uses downsampling."
  })
  public Serializers SERIALIZER = Serializers.LEGACY_AMPERSAND;
  public String PREFIX = "DeltaNetwork &6>>&f";

  @Create
  public Brigadier BRIGADIER;

  public static class Brigadier {

    public String PLAYERS_ONLY = "{PRFX} Только игроки могут использовать эту команду.";
    @Placeholders("{NICKNAME}")
    public String PLAYER_NOT_FOUND = "{PRFX} Игрок '{NICKNAME}' не найден.";
    @Placeholders("{SYNTAX}")
    public String COMMAND_SYNTAX = "{PRFX} &7{SYNTAX}&4 <- ЗДЕСЬ";
    @Placeholders("{PART}")
    public String COMMAND_INCORRECT_PART = "&c&n{PART}";

    // Mojang translation begin here \/
    @Placeholders({"{MIN}", "{CURRENT}"})
    public String DOUBLE_TOO_LOW = "{PRFX} Вещественное число двойной точности должно быть не меньше {MIN}; обнаружено {CURRENT}";
    @Placeholders({"{MAX}", "{CURRENT}"})
    public String DOUBLE_TOO_HIGH = "{PRFX} Вещественное число двойной точности должно быть не больше {MAX}; обнаружено {CURRENT}";

    @Placeholders({"{MIN}", "{CURRENT}"})
    public String FLOAT_TOO_LOW = "{PRFX} Вещественное число должно быть не меньше {MIN}; обнаружено {CURRENT}";
    @Placeholders({"{MAX}", "{CURRENT}"})
    public String FLOAT_TOO_HIGH = "{PRFX} Вещественное число должно быть не больше {MAX}; обнаружено {CURRENT}";

    @Placeholders({"{MIN}", "{CURRENT}"})
    public String INTEGER_TOO_LOW = "{PRFX} Целое число должно быть не меньше {MIN}; обнаружено {CURRENT}";
    @Placeholders({"{MAX}", "{CURRENT}"})
    public String INTEGER_TOO_HIGH = "{PRFX} Целое число должно быть не больше {MAX}; обнаружено {CURRENT}";

    @Placeholders({"{MIN}", "{CURRENT}"})
    public String LONG_TOO_LOW = "{PRFX} Длинное целое число должно быть не меньше {MIN}; обнаружено {CURRENT}";
    @Placeholders({"{MAX}", "{CURRENT}"})
    public String LONG_TOO_HIGH = "{PRFX} Длинное целое число должно быть не больше {MAX}; обнаружено {CURRENT}";

    @Placeholders("{LITERAL}")
    public String LITERAL_INCORRECT = "{PRFX} Ожидался параметр {LITERAL}";
    public String READER_EXPECTED_START_OF_QUOTE = "{PRFX} Ожидалась кавычка в начале строки";
    public String READER_EXPECTED_END_OF_QUOTE = "{PRFX} Ожидалась кавычка в конце строки";

    @Placeholders("{ESCAPE}")
    public String READER_INVALID_ESCAPE = "{PRFX} Неверная экранированная последовательность «{ESCAPE}» в строке, заключённой в кавычки";

    @Placeholders("{VALUE}")
    public String READER_INVALID_BOOL = "{PRFX} Неверное логическое значение; ожидалось «true» или «false», а обнаружено «{VALUE}»";
    public String READER_EXPECTED_BOOL = "{PRFX} Ожидалось логическое значение";

    @Placeholders("{VALUE}")
    public String READER_INVALID_INT = "{PRFX} Неверное целое число «{VALUE}»";
    public String READER_EXPECTED_INT = "{PRFX} Ожидалось целое число";

    @Placeholders("{VALUE}")
    public String READER_INVALID_LONG = "{PRFX} Неверное длинное целое число «{VALUE}»";
    public String READER_EXPECTED_LONG = "{PRFX} Ожидалось длинное целое число";

    @Placeholders("{VALUE}")
    public String READER_INVALID_DOUBLE = "{PRFX} Неверное вещественное число двойной точности «{VALUE}»";
    public String READER_EXPECTED_DOUBLE = "{PRFX} Ожидалось вещественное число двойной точности";

    @Placeholders("{VALUE}")
    public String READER_INVALID_FLOAT = "{PRFX} Неверное вещественное число «{VALUE}»";
    public String READER_EXPECTED_FLOAT = "{PRFX} Ожидалось вещественное число";

    @Placeholders("{VALUE}")
    public String READER_EXPECTED_SYMBOL = "{PRFX} Ожидалось «{VALUE}»";

    public String DISPATCHER_UNKNOWN_COMMAND = "{PRFX} Неизвестная или неполная команда; см. ниже, чтобы найти ошибку»";
    public String DISPATCHER_UNKNOWN_ARGUMENT = "{PRFX} Неверный аргумент для команды";

    public String DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR = "{PRFX} Ожидался пробел, разделяющий аргументы, но он был пропущен";

    @Placeholders("{VALUE}")
    public String DISPATCHER_PARSE_EXCEPTION = "{PRFX} Не удалось распознать команду: {VALUE}";

    public String COMMAND_FAILED = "{PRFX} При выполнении команды произошла непредвиденная ошибка";
  }
}
