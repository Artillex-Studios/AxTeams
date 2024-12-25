package com.artillexstudios.axteams.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.config.annotation.Named;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.LogUtils;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axteams.AxTeamsPlugin;
import com.artillexstudios.axteams.utils.FileUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Language implements ConfigurationPart {
    private static final Path LANGUAGE_DIRECTORY = FileUtils.PLUGIN_DIRECTORY.resolve("language");
    private static final Language INSTANCE = new Language();

    public static class Reload implements ConfigurationPart {
        public static String success = "<#00FF00>Successfully reloaded the configurations of the plugin in <white><time></white>ms!";
        public static String fail = "<#FF0000>There were some issues while reloading file(s): <white><files></white>! Please check out the console for more information! <br>Reload done in: <white><time></white>ms!";
    }

    public static class Error implements ConfigurationPart {
        public static String userNotLoaded = "<#FF0000>Your userdata has not loaded yet! Please try again in a moment!";
        public static String notInTeam = "<#FF0000>You are not in a team!";
        public static String noPermission = "<#FF0000>You are not permitted to do this!";
        public static String notLeader = "<#FF0000>You are not the leader of this team!";
        public static String unknownMember = "<#FF0000>A member named <name> does not exist!";
        public static String alreadyInTeam = "<#FF0000>You are already a member of a team!";
        public static String alreadyExists = "<#FF0000>A team named <name> already exists! Try something more original!";
        public static String failedToCreate = "<#FF0000>Failed to create your team, as no owner group is set up! Please, notify server administrators!";
        public static String leader = "<#FF0000>You are the leader of this team! You can't leave it. You can however disband it with /team disband!";
        public static String noHomeSet = "<#FF0000>Your team does not have a home set!";
        public static String notFound = "<#FF0000>No team named <name> was found!";

        public static class TeamNaming implements ConfigurationPart {
            public static String tooShort = "<#FF0000>The name<white><name></white> is too short!";
            public static String tooLong = "<#FF0000>The name<white><name></white> is too short!";
            public static String blacklisted = "<#FF0000>The name<white><name></white> is blacklisted! Not cool!";
            public static String notWhitelisted = "<#FF0000>The name<white><name></white> is not allowed!";
        }
    }

    public static class ChatFormat implements ConfigurationPart {
        public static String team = "<#FF0000>Team <white>| <group_display_name> <player> <gray>» <white><message>";
        public static String ally = "<#03BAFC>Ally <white>| <team_display_name> <group_display_name> <player> <gray>» <white><message>";
    }

    public static class Success implements ConfigurationPart {
        public static String created = "<#00FF00>Successfully created your team with name <white><name></white>!";
        public static String disbanded = "<#00FF00>You have successfully disbanded your team!";
        public static String disbandConfirmation = "<#FF0000>Are you sure you want to <u>disband</u> your team? <white>If yes, repeat this command!";
        public static String leave = "<#FF0000>You have left the team!";
        public static String leaveAnnouncement = "<#FF0000><white><player></white> has left the team!";
        public static String teleportHome = "<white>You were successfully teleported to your team's home!";
        public static String setHome = "<white>You have successfully set your team's home!";
        public static String transfer = "<#00FF00>You have successfully transferred the team to <player>!";
        public static String transferAnnouncement = "<#00FF00>The team has been transferred from <from> to <to>!";
        public static String kicked = "<#00FF00>You have successfully kicked <player> from the team!";

        public static class ChatToggle implements ConfigurationPart {
            public static class AllyChat implements ConfigurationPart {
                public static String enable = "<#00FF00>You have successfully enabled ally chat! From now on, you'll be talking in the ally chat!";
                public static String disable = "<#FF0000>You have successfully disabled ally chat! From now on, you'll be talking in the server chat!";
            }

            public static class TeamChat implements ConfigurationPart {
                public static String enable = "<#00FF00>You have successfully enabled team chat! From now on, you'll be talking in the team chat!";
                public static String disable = "<#FF0000>You have successfully disabled team chat! From now on, you'll be talking in the server chat!";
            }
        }

        @Named("pvp-toggle")
        public static class PvPToggle implements ConfigurationPart {
            public static String enable = "<#00FF00>You have successfully enabled team pvp!";
            public static String disable = "<#FF0000>You have successfully disabled team pvp!";
        }
    }

    public static class Warp implements ConfigurationPart {
        public static String notFound = "<#FF0000>Could not find warp named <white><name></white>!";
        public static String teleported = "<#00FF00>You have successfully teleported to warp <name>!";
        public static String hasPassword = "<#FF0000>Warp <name> requires a password!";
        public static String incorrectPassword = "<#FF0000>Incorrect password for warp <name>!";
        public static String limitReached = "<#FF0000>Your team has leached its warp limit! (<warps>/<limit>)";
        public static String created = "<#00FF00>You have successfully created a new warp named <name>!";
        public static String alreadyExists = "<#FF0000>A warp with name <name> already exists!";
    }

    public static class Invite implements ConfigurationPart {
        public static String alreadyMember = "<#00FF00><player> is already a member of the team!";
        public static String alreadyInvited = "<#FF000><player> has already been invited to the team!";
        public static String inviteSent = "<#00FF00><player> has been invited to join the team!";
        public static String inviteReceived = "<#00FF00><player> would like to ask you to join <team>!";
        public static String inviteSentAnnouncement = "<#00FF00><player> invited <other> into the team!";
        public static String inviteAcceptedAnnouncement = "<#00FF00><player> accepted the invite and joined the team!";
        public static String inviteAccepted = "<#00FF00>You have successfully accepted the invite and joined the team!";
        public static String notInvited = "<#00FF00>You are not invited to join <team>!";
        public static String teamIsFull = "<#FF0000>The team is full!";
    }

    public static class Ally implements ConfigurationPart {
        public static String alreadyAllies = "<#00FF00><team> is already an ally of the team!";
        public static String notOnline = "<#00FF00>nobody from <team> is online, so we can't track allies!";
        public static String alreadyInvited = "<#FF000><team> has already been invited to become an ally!";
        public static String inviteSent = "<#00FF00><team> has been invited to become an ally!";
        public static String inviteReceived = "<#00FF00><player> would like to ask you to become allies with <team>!";
        public static String inviteSentAnnouncement = "<#00FF00><player> invited <team> to become an ally!";
        public static String inviteAcceptedAnnouncement = "<#00FF00><team> accepted the ally request and become an ally!";
        public static String inviteAccepted = "<#00FF00>You have successfully accepted the ally invite and became an ally of <team>!";
        public static String notInvited = "<#00FF00>Your team is not invited to be an ally of <team>!";
        public static String allyLimitReached = "<#FF0000>The ally limit has been reached!";
    }

    @Comment("The prefix we should use before messages sent by the plugin.")
    public static String prefix = "<b><gradient:#CB2D3E:#EF473A>AxTeams</gradient></b> ";




//    public static String DISBANDED = "Disbanded";
//    public static String ALREADY_IN_TEAM = "In team";
//    public static String ALREADY_EXISTS = "Exists";
//    public static String TOO_SHORT = "Short";
//    public static String TOO_LONG = "Long";
//    public static String BLACKLISTED = "Blacklisted";
//    public static String NOT_WHITELISTED = "not whitelisted";
//    public static String CREATED = "created";

    public static String lastLanguage;
    private YamlConfiguration config = null;

    public static boolean reload() {
        if (Config.debug) {
            LogUtils.debug("Reload called on language!");
        }
        FileUtils.copyFromResource("language");

        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        if (Config.debug) {
            LogUtils.debug("Refreshing language");
        }
        Path path = LANGUAGE_DIRECTORY.resolve(Config.language + ".yml");
        boolean shouldDefault = false;
        if (Files.exists(path)) {
            if (Config.debug) {
                LogUtils.debug("File exists");
            }
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        } else {
            shouldDefault = true;
            path = LANGUAGE_DIRECTORY.resolve("en_US.yml");
            LogUtils.error("No language configuration was found with the name {}! Defaulting to en_US...", Config.language);
        }

        // The user might have changed the config
        if (this.config == null || (lastLanguage != null && lastLanguage.equalsIgnoreCase(Config.language))) {
            lastLanguage = shouldDefault ? "en_US" : Config.language;
            if (Config.debug) {
                LogUtils.debug("Set lastLanguage to {}", lastLanguage);
            }
            InputStream defaults = AxTeamsPlugin.instance().getResource("language/" + lastLanguage + ".yml");
            if (defaults == null) {
                if (Config.debug) {
                    LogUtils.debug("Defaults are null, defaulting to en_US.yml");
                }
                defaults = AxTeamsPlugin.instance().getResource("language/en_US.yml");
            }

            if (Config.debug) {
                LogUtils.debug("Loading config from file {} with defaults {}", path, defaults);
            }

            this.config = YamlConfiguration.of(path, Language.class)
                    .configVersion(1, "config-version")
                    .withDefaults(defaults)
                    .withDumperOptions(options -> {
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                    }).build();
        }

        this.config.load();
        return true;
    }
}
