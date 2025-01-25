package com.artillexstudios.axteams.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.config.annotation.Ignored;
import com.artillexstudios.axapi.config.annotation.Named;
import com.artillexstudios.axapi.config.annotation.Serializable;
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
    @Comment("The prefix we should use before messages sent by the plugin.")
    public static String prefix = "<b><gradient:#CB2D3E:#EF473A>AxTeams</gradient></b> ";

    public static Reload reload = new Reload();
    public static Error error = new Error();
    public static ChatFormat chatFormat = new ChatFormat();
    public static Success success = new Success();
    public static Warp warp = new Warp();
    public static Invite invite = new Invite();
    public static Ally ally = new Ally();
    public static Placeholder placeholder = new Placeholder();

    @Serializable
    public static class Reload {
        public String success = "<#00FF00>Successfully reloaded the configurations of the plugin in <white><time></white>ms!";
        public String fail = "<#FF0000>There were some issues while reloading file(s): <white><files></white>! Please check out the console for more information! <br>Reload done in: <white><time></white>ms!";
    }

    @Serializable
    public static class Error {
        public String userNotLoaded = "<#FF0000>Your userdata has not loaded yet! Please try again in a moment!";
        public String notInTeam = "<#FF0000>You are not in a team!";
        public String noPermission = "<#FF0000>You are not permitted to do this!";
        public String notLeader = "<#FF0000>You are not the leader of this team!";
        public String unknownMember = "<#FF0000>A member named <name> does not exist!";
        public String alreadyInTeam = "<#FF0000>You are already a member of a team!";
        public String alreadyExists = "<#FF0000>A team named <name> already exists! Try something more original!";
        public String failedToCreate = "<#FF0000>Failed to create your team, as no owner group is set up! Please, notify server administrators!";
        public String leader = "<#FF0000>You are the leader of this team! You can't leave it. You can however disband it with /team disband!";
        public String noHomeSet = "<#FF0000>Your team does not have a home set!";
        public String notFound = "<#FF0000>No team named <name> was found!";
        public String groupAlreadyExists = "<#FF0000>Group <group> already exists!";
        public String hasMembers = "<#FF0000>This group has members, so it can't be deleted!";
        public String ownerGroup = "<#FF0000>This group is the owner group, so it can't be deleted!";
        public String defaultGroup = "<#FF0000>This group is the default group, so it can't be deleted!";
        public String notEnoughBankBalance = "<#FF0000>There is not enough money in the bank to take!";
        public String notEnoughToDeposit = "<#FF0000>You don't have enough money to deposit!";
        public TeamNaming teamNaming = new TeamNaming();

        @Serializable
        public static class TeamNaming {
            public String tooShort = "<#FF0000>The name <white><name></white> is too short!";
            public String tooLong = "<#FF0000>The name <white><name></white> is too short!";
            public String blacklisted = "<#FF0000>The name <white><name></white> is blacklisted! Not cool!";
            public String notWhitelisted = "<#FF0000>The name <white><name></white> is not allowed!";
        }
    }

    @Serializable
    public static class ChatFormat {
        public String team = "<#FF0000>Team <white>| <group_display_name> <player> <gray>» <white><message>";
        public String ally = "<#03BAFC>Ally <white>| <team_display_name> <group_display_name> <player> <gray>» <white><message>";
    }

    @Serializable
    public static class Success {
        public String created = "<#00FF00>Successfully created your team with name <white><name></white>!";
        public String disbanded = "<#00FF00>You have successfully disbanded your team!";
        public String disbandConfirmation = "<#FF0000>Are you sure you want to <u>disband</u> your team? <white>If yes, repeat this command!";
        public String leave = "<#FF0000>You have left the team!";
        public String leaveAnnouncement = "<#FF0000><white><player></white> has left the team!";
        public String teleportHome = "<white>You were successfully teleported to your team's home!";
        public String setHome = "<white>You have successfully set your team's home!";
        public String transfer = "<#00FF00>You have successfully transferred the team to <player>!";
        public String transferAnnouncement = "<#00FF00>The team has been transferred from <from> to <to>!";
        public String kicked = "<#00FF00>You have successfully kicked <player> from the team!";
        public String groupChangedForOther = "<#00FF00>You have changed <player>'s group to <group>!";
        public String groupChanged = "<#00FF00>Your group has been changed to <group>!";
        public String displayNameChanged = "<#00FF00>You have successfully changed the team's displayname to <name>!";
        public String displayNameChangedAnnouncement = "<#00FF00><player> change the team's display name from <old-name> to <name>!";
        public String groupCreated = "<#00FF00><group>has been created!";
        public String bankBalance = "<#00FF00>Current bank balance: <balance>$";
        public String bankBalanceTake = "<#00FF00>Took out: <amount> current bank balance: <balance>$";
        public String bankBalanceDeposit = "<#00FF00>Deposited: <amount> current bank balance: <balance>$";
        public ChatToggle chatToggle = new ChatToggle();
        @Named("pvp-toggle")
        public PvPToggle pvPToggle = new PvPToggle();

        @Serializable
        public static class ChatToggle {
            public AllyChat allyChat = new AllyChat();
            public TeamChat teamChat = new TeamChat();

            @Serializable
            public static class AllyChat {
                public String enable = "<#00FF00>You have successfully enabled ally chat! From now on, you'll be talking in the ally chat!";
                public String disable = "<#FF0000>You have successfully disabled ally chat! From now on, you'll be talking in the server chat!";
            }

            @Serializable
            public static class TeamChat {
                public String enable = "<#00FF00>You have successfully enabled team chat! From now on, you'll be talking in the team chat!";
                public String disable = "<#FF0000>You have successfully disabled team chat! From now on, you'll be talking in the server chat!";
            }
        }

        @Serializable
        public static class PvPToggle {
            public String enable = "<#00FF00>You have successfully enabled team pvp!";
            public String disable = "<#FF0000>You have successfully disabled team pvp!";
        }
    }

    @Serializable
    public static class Warp {
        public String notFound = "<#FF0000>Could not find warp named <white><name></white>!";
        public String teleported = "<#00FF00>You have successfully teleported to warp <name>!";
        public String hasPassword = "<#FF0000>Warp <name> requires a password!";
        public String incorrectPassword = "<#FF0000>Incorrect password for warp <name>!";
        public String limitReached = "<#FF0000>Your team has leached its warp limit! (<warps>/<limit>)";
        public String created = "<#00FF00>You have successfully created a new warp named <name>!";
        public String alreadyExists = "<#FF0000>A warp with name <name> already exists!";
    }

    @Serializable
    public static class Invite {
        public String alreadyMember = "<#00FF00><player> is already a member of the team!";
        public String alreadyInvited = "<#FF000><player> has already been invited to the team!";
        public String inviteSent = "<#00FF00><player> has been invited to join the team!";
        public String inviteReceived = "<#00FF00><player> would like to ask you to join <team>!";
        public String inviteSentAnnouncement = "<#00FF00><player> invited <other> into the team!";
        public String inviteAcceptedAnnouncement = "<#00FF00><player> accepted the invite and joined the team!";
        public String inviteAccepted = "<#00FF00>You have successfully accepted the invite and joined the team!";
        public String notInvited = "<#00FF00>You are not invited to join <team>!";
        public String teamIsFull = "<#FF0000>The team is full!";
    }

    @Serializable
    public static class Ally {
        public String alreadyAllies = "<#00FF00><team> is already an ally of the team!";
        public String notOnline = "<#00FF00>nobody from <team> is online, so we can't track allies!";
        public String alreadyInvited = "<#FF000><team> has already been invited to become an ally!";
        public String inviteSent = "<#00FF00><team> has been invited to become an ally!";
        public String inviteReceived = "<#00FF00><player> would like to ask you to become allies with <team>!";
        public String inviteSentAnnouncement = "<#00FF00><player> invited <team> to become an ally!";
        public String inviteAcceptedAnnouncement = "<#00FF00><team> accepted the ally request and become an ally!";
        public String inviteAccepted = "<#00FF00>You have successfully accepted the ally invite and became an ally of <team>!";
        public String notInvited = "<#00FF00>Your team is not invited to be an ally of <team>!";
        public String allyLimitReached = "<#FF0000>The ally limit has been reached!";
        public String hasIncomingRequest = "<#FF0000>You already have an incoming request from this team! Use /team ally accept <team> to accept!";
        public String removed = "<#FF0000>You removed <team> from your allies!";
        public String removedAnnouncement = "<#FF0000><team> and your team are no longer allies!";
    }

    @Serializable
    public static class Placeholder {
        public String noTeam = "No team!";
    }

    public static String enderChestTitle = "<#00FF00>EnderChest";

    @Comment("Do not touch!")
    public static int configVersion = 1;

    @Ignored
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
                        options.setSplitLines(false);
                    }).build();
        }

        this.config.load();
        return true;
    }
}
