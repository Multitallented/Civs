package org.redcastlemedia.multitallented.civs.update;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;

public final class Update1d5d6 {
    private Update1d5d6() {

    }

    public static String update() {
        updateConfig();
        updateItemTypes();
        return "1.5.6";
    }

    private static void updateItemTypes() {
        File itemTypesFolder = new File(Civs.getInstance().getDataFolder(), "item-types");
        if (!itemTypesFolder.exists()) {
            return;
        }
        upgradeTowns(itemTypesFolder);
        addJammerTrap(itemTypesFolder);
        updateEmbassy(itemTypesFolder);
        updateDefenses(itemTypesFolder);
        updateNPCHousing(itemTypesFolder);
        updateTranslations();
        updateGovTypes();

        File adminFolder = new File(itemTypesFolder, "admin-invisible");
        if (adminFolder.exists()) {
            File shelterFile = new File(adminFolder, "shelter.yml");
            if (shelterFile.exists()) {
                try {
                    FileConfiguration config = new YamlConfiguration();
                    config.load(shelterFile);
                    List<String> effects = config.getStringList("effects");
                    effects.add("bed");
                    config.set("effects", effects);
                    config.save(shelterFile);
                } catch (Exception e) {

                }
            }
        }

    }

    private static void updateGovTypes() {
        File govTypesFolder = new File(Civs.getInstance().getDataFolder(), "gov-types");
        if (!govTypesFolder.exists()) {
            return;
        }
        for (File file : govTypesFolder.listFiles()) {
            if ("tribalism.yml".equals(file.getName()) ||
                    "dictatorship.yml".equals(file.getName()) ||
                    "socialism.yml".equals(file.getName()) ||
                    "feudalism.yml".equals(file.getName())) {
                continue;
            }
            FileConfiguration config = new YamlConfiguration();
            try {
                config.load(file);
                if (config.isSet("transition")) {
                    for (String key : config.getConfigurationSection("transition").getKeys(false)) {
                        if (!"ANARCHY".equals(config.getString("transition." + key + ".to")) &&
                                !"KRATEROCRACY".equals(config.getString("transition." + key + ".to"))) {
                            continue;
                        }
                        if (config.isSet("transition." + key + ".inactive")) {
                            continue;
                        }
                        config.set("transition." + key + ".to", "IDIOCRACY");
                        break;
                    }
                }
                config.save(file);
            } catch (Exception e) {

            }
        }
        File idiocracyFile = new File(govTypesFolder, "idiocracy.yml");
        if (!idiocracyFile.exists()) {
            try {
                idiocracyFile.createNewFile();
                FileConfiguration config = new YamlConfiguration();
                config.set("icon", "DEAD_BUSH");
                config.set("enabled", true);
                config.set("transition.0.revolt", 51);
                config.set("transition.0.power", 30);
                config.set("transition.0.to", "KRATEROCRACY");
                config.set("transition.1.to", "LIBERTARIAN");
                config.set("transition.1.revolt", 51);

                config.set("buffs.cost.percent", 25);
                List<String> groups = new ArrayList<>();
                groups.add("allhousing");
                groups.add("utility");
                groups.add("mine");
                groups.add("quarry");
                groups.add("factory");
                groups.add("farm");
                config.set("buffs.cost.groups", groups);
                config.save(idiocracyFile);

            } catch (Exception e) {

            }
        }
    }

    private static void updateTranslations() {
        File translationsFolder = new File(Civs.getInstance().getDataFolder(), "translations");
        if (!translationsFolder.exists()) {
            return;
        }
        File enFile = new File(translationsFolder, "en.yml");
        if (enFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(enFile);
                config.set("deposit-money", "You've deposited $1 into $2's bank");
                config.set("too-much-money", "You can't deposit more than $1 in a bank");
                config.set("wild", "Wilderness");
                config.set("siege-built", "WARNING! $1 has created a $2 targeting $3");
                config.set("jammer_trap-name", "Jammer Trap");
                config.set("jammer_trap-desc", "A building that intercepts teleports that would travel within 100. Redirects the destination to nearby the jammer.");
                config.set("jammer-redirect", "@{RED}[ALERT] Your teleport was intercepted by a $1");
                config.set("no-tp-out-of-town", "You can't teleport out of a non-allied town");
                config.set("intruder-enter", "@{RED}[WARNING] $1 has entered $2");
                config.set("intruder-exit", "$1 has exited $2");
                config.set("raid-porter-offline", "You cant raid $1 when none of their members are online.");
                config.set("no-blocks-above-chest", "There must be no blocks above the center chest of a $1");
                config.set("activate-anticamp-question", "$1 has died in $2. Would you like to activate anti-camping defenses for $3?");
                config.set("idiocracy-name", "Idiocracy");
                config.set("idiocracy-desc", "Whoever shoots the most fireworks, and spams the most signs with their name on them becomes the owner.");
                config.save(enFile);
            } catch (Exception e) {

            }
        }
        File deFile = new File(translationsFolder, "de.yml");
        if (deFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(deFile);
                config.set("deposit-money", "Sie haben $1 auf $2s Bank eingezahlt");
                config.set("too-much-money", "Sie können nicht mehr als $1 auf eine Bank einzahlen");
                config.set("wild", "Wildnis");
                config.set("siege-built", "@{RED}[WARNUNG] $1 hat ein $2 erstellt, das auf $3 abzielt");
                config.set("jammer_trap-name", "Abfangjäger-Falle");
                config.set("jammer_trap-desc", "Ein Gebäude, das Teleports abfängt, die sich innerhalb von 100 Blöcken bewegen würden. Leiten Sie das Ziel zur nahe gelegenen Falle um.");
                config.set("jammer-redirect", "@{RED}[WARNEN] Dein Teleport wurde von einem $1 abgefangen");
                config.set("no-tp-out-of-town", "Sie können nicht teleportieren, um einer unfreundlichen Stadt zu entkommen");
                config.set("intruder-enter", "@{RED}[WARNUNG] $1 ist in $2 angekommen");
                config.set("intruder-exit", "$1 hat $2 verlassen");
                config.set("raid-porter-offline", "Sie können nicht in $1 marschieren, es sei denn, Ihre Mitglieder sind online.");
                config.set("activate-anticamp-question", "$1 ist in $2 gestorben. Möchten Sie die Verteidigung von Campern für $3 aktivieren?");
                config.set("idiocracy-name", "Idiokratie");
                config.set("idiocracy-desc", "Wenn Sie die meisten Feuerwerke schießen und die meisten Zeichen mit Ihrem Namen setzen, werden Sie der Inhaber.");
                config.save(deFile);
            } catch (Exception e) {

            }
        }
        File esFile = new File(translationsFolder, "es.yml");
        if (esFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(esFile);
                config.set("deposit-money", "Has depositado $1 en el banco de $2");
                config.set("too-much-money", "No puede depositar más de $1 en un banco");
                config.set("wild", "Desierto");
                config.set("siege-built", "@{RED}[ALERTA] $1 ha creado una $2 dirigida a $3");
                config.set("jammer_trap-name", "Trampa de Interceptador");
                config.set("jammer_trap-desc", "Un edificio que intercepta telepuertos que viajarían dentro de 100 cuadras. Redirige el destino a la trampa cercana.");
                config.set("jammer-redirect", "@{RED}[ALERTA] Su teletransporte fue interceptado por un $1");
                config.set("no-tp-out-of-town", "No puedes teletransportarte para escapar de una ciudad no aliada");
                config.set("intruder-enter", "@{RED}[ADVERTENCIA]$1 ha entrado en $2");
                config.set("intruder-exit", "$1 ha salido de $2");
                config.set("raid-porter-offline", "No puede invadir $1 cuando ninguno de sus miembros está en línea.");
                config.set("activate-anticamp-question", "$1 ha muerto en $2. ¿Te gustaría activar las defensas contra el camping en $3?");
                config.set("idiocracy-name", "Idiocracia");
                config.set("idiocracy-desc", "Quien arroja la mayor cantidad de fuegos artificiales y coloca la mayor cantidad de letreros con su nombre se convierte en el propietario.");
                config.save(esFile);
            } catch (Exception e) {

            }
        }
        File ptBrFile = new File(translationsFolder, "pt_br.yml");
        if (ptBrFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(ptBrFile);
                config.set("deposit-money", "Você depositou $1 no banco de $2");
                config.set("too-much-money", "Você não pode depositar mais que $1 em um banco");
                config.set("wild", "Selvagem");
                config.set("siege-built", "@{RED}[ALERTA] $1 ha creado una $2 dirigida a $3");
                config.set("jammer_trap-name", "Armadilha de Disrupção");
                config.set("jammer_trap-desc", "Uma construção que intercepta teletransportes que viajariam em um raio de 100 blocos. Redireciona o destino para perto da armadilha.");
                config.set("jammer-redirect", "@{RED}[ALERTA] Seu teletransporte foi interceptado por $1");
                config.set("no-tp-out-of-town", "Você não pode teletransportar fora de uma vila aliada");
                config.set("intruder-enter", "@{RED[ALERTA] $1 entrou em $2");
                config.set("intruder-exit", "$1 saiu de $2");
                config.set("raid-porter-offline", "Você não pode saquear $1 enquanto nenhum de seus membros esta online.");
                config.set("activate-anticamp-question", "$1 morreu em $2. Você gostaria de ativar as defesas anti-camp por $3?");
                config.set("idiocracy-name", "Idiocracia");
                config.set("idiocracy-desc", "Qualquer um que atire mais fogos de artificio e faça mais placas com seu nome vira o dono.");
                config.save(ptBrFile);
            } catch (Exception e) {

            }
        }
        File huFile = new File(translationsFolder, "hu.yml");
        if (huFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(huFile);
                config.set("deposit-money", "A $1-at $2 bankjába helyezték");
                config.set("too-much-money", "A betét limit $1");
                config.set("wild", "Vadon");
                config.set("siege-built", "@{RED}FIGYELEM! $1 létrehozott egy $2-t aminek a célpontja $3");
                config.set("jammer_trap-name", "Fogócsapda");
                config.set("jammer_trap-desc", "Egy épület, amely megváltoztatja a teleport célállomását.");
                config.set("jammer-redirect", "@{RED}[FIGYELEM] Teleportját az $1 módosította");
                config.set("no-tp-out-of-town", "Nem lehet teleportálni nem szövetséges városból.");
                config.set("intruder-enter", "@{RED}[FIGYELEM] $1 belépett az $2-be");
                config.set("intruder-exit", "$1 elhagyta az $2-t");
                config.set("raid-porter-offline", "Nem támadhatja meg az $1-t, ha egyetlen tag sem online.");
                config.set("activate-anticamp-question", "$1 meghalt az $2-n. Bekapcsolja az $3 kempingvédelmet?");
                config.set("idiocracy-name", "Idiocracy");
                config.set("idiocracy-desc", "Aki egy rögzített tűzijátékot tartalmaz, és egy további hirdetést készített egy nevükkel, az lesz a tulajdonos.");
                config.save(huFile);
            } catch (Exception e) {

            }
        }
        File zhFile = new File(translationsFolder, "zh.yml");
        if (zhFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(zhFile);
                config.set("deposit-money", "您将$1存入$2的银行");
                config.set("too-much-money", "您在银行的存款不能超过$1");
                config.set("wild", "荒原");
                config.set("jammer_trap-name", "拦截陷阱");
                config.set("jammer_trap-desc", "拦截传输端口的建筑物，该端口将在100个街区内移动。 将目标重定向到附近的位置。");
                config.set("jammer-redirect", "@{RED}[警报] 您的转帐由$1重定向");
                config.set("no-tp-out-of-town", "您无法将自己转移出敌人的城镇");
                config.set("intruder-enter", "@{RED}[警报] $1已进入$2");
                config.set("intruder-exit", "$1已退出$2");
                config.set("raid-porter-offline", "当成员不在线时，您无法突袭$1");
                config.set("activate-anticamp-question", "$1已在$2内部死亡。 您要激活$3的野营防御吗？");
                config.set("idiocracy-name", "专制");
                config.set("idiocracy-desc", "发射最多烟火并产生最多广告的人将成为该城市的所有者。 广告中必须包含您的姓名。");
                config.save(zhFile);
            } catch (Exception e) {

            }
        }
        File viFile = new File(translationsFolder, "vi.yml");
        if (viFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(viFile);
                config.set("deposit-money", "Bạn đã gửi $1 vào ngân hàng $2");
                config.set("too-much-money", "Bạn không thể gửi nhiều hơn $1 trong một ngân hàng");
                config.set("wild", "đất bỏ hoang");
                config.set("jammer_trap-name", "Đánh chặn bẩy");
                config.set("jammer_trap-desc", "Chuyển hướng nếu di chuyển trong vòng 100 khối của tòa nhà. Chuyển hướng đến một nơi nào đó gần đánh chặn.");
                config.set("jammer-redirect", "@{RED}[CẢNH BÁO] Dịch chuyển tức thời của bạn đã bị chặn bởi $1");
                config.set("no-tp-out-of-town", "Bạn không thể dịch chuyển ra khỏi một thị trấn không liên minh");
                config.set("intruder-enter", "@{RED}[CẢNH BÁO] $1 đã vào $2");
                config.set("intruder-exit", "$1 đã thoát $2");
                config.set("raid-porter-offline", "Bạn không thể xâm chiếm $1 mà không có bất kỳ thành viên nào của họ trực tuyến.");
                config.set("activate-anticamp-question", "$1 đã chết trong $2. Bạn có muốn kích hoạt hệ thống phòng thủ cắm trại cho $2?");
                config.set("idiocracy-name", "Dân chủ");
                config.set("idiocracy-desc", "Bất cứ ai bắn nhiều pháo hoa nhất, và tạo ra nhiều dấu hiệu nhất với tên của họ trên đó, sẽ trở thành chủ sở hữu.");
                config.save(viFile);
            } catch (Exception e) {

            }
        }
    }

    private static void updateNPCHousing(File itemTypesFolder) {
        File npchousingFolder = new File(itemTypesFolder, "npchousing");
        if (!npchousingFolder.exists()) {
            return;
        }
        File npcChaletFile = new File(npchousingFolder, "npc_chalet.yml");
        if (npcChaletFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(npcChaletFile);
                List<String> rebuildList = new ArrayList<>();
                rebuildList.add("basechalet");
                rebuildList.add("npc_house");
                config.set("rebuild", rebuildList);
                List<String> preReqs = config.getStringList("pre-reqs");
                if (preReqs.remove("basechalet:built=1")) {
                    preReqs.add("basechalet:built=1|npc_house:built=1");
                    config.set("pre-reqs", preReqs);
                }
                config.save(npcChaletFile);
            } catch (Exception e) {

            }
        }
        File npcDwellingFile = new File(npchousingFolder, "npc_dwelling.yml");
        if (npcDwellingFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(npcDwellingFile);
                List<String> rebuildList = new ArrayList<>();
                rebuildList.add("basedwelling");
                rebuildList.add("npc_hovel");
                config.set("rebuild", rebuildList);
                List<String> preReqs = config.getStringList("pre-reqs");
                if (preReqs.remove("basedwelling:built=1")) {
                    preReqs.add("basedwelling:built=1|npc_hovel:built=1");
                    config.set("pre-reqs", preReqs);
                }
                config.save(npcDwellingFile);
            } catch (Exception e) {

            }
        }
        File npcHouseFile = new File(npchousingFolder, "npc_house.yml");
        if (npcHouseFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(npcHouseFile);
                List<String> rebuildList = new ArrayList<>();
                rebuildList.add("basehouse");
                rebuildList.add("npc_dwelling");
                config.set("rebuild", rebuildList);
                List<String> preReqs = config.getStringList("pre-reqs");
                if (preReqs.remove("basehouse:built=1")) {
                    preReqs.add("basehouse:built=1|npc_dwelling:built=1");
                    config.set("pre-reqs", preReqs);
                }
                config.save(npcHouseFile);
            } catch (Exception e) {

            }
        }
        File npcHovelFile = new File(npchousingFolder, "npc_hovel.yml");
        if (npcHovelFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(npcHovelFile);
                List<String> rebuildList = new ArrayList<>();
                rebuildList.add("basehovel");
                rebuildList.add("npc_shack");
                config.set("rebuild", rebuildList);
                List<String> preReqs = config.getStringList("pre-reqs");
                if (preReqs.remove("basehovel:built=1")) {
                    preReqs.add("basehovel:built=1|npc_shack:built=1");
                    config.set("pre-reqs", preReqs);
                }
                config.save(npcHovelFile);
            } catch (Exception e) {

            }
        }
        File npcMansionFile = new File(npchousingFolder, "npc_mansion.yml");
        if (npcMansionFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(npcMansionFile);
                List<String> rebuildList = new ArrayList<>();
                rebuildList.add("basemansion");
                rebuildList.add("npc_chalet");
                config.set("rebuild", rebuildList);
                List<String> preReqs = config.getStringList("pre-reqs");
                if (preReqs.remove("basemansion:built=1")) {
                    preReqs.add("basemansion:built=1|npc_chalet:built=1");
                    config.set("pre-reqs", preReqs);
                }
                config.save(npcMansionFile);
            } catch (Exception e) {

            }
        }
    }

    private static void updateEmbassy(File itemTypesFolder) {
        File utilitiesFolder = new File(itemTypesFolder, "utilities");
        if (!utilitiesFolder.exists()) {
            return;
        }
        File embassyFile = new File(utilitiesFolder, "embassy.yml");
        if (embassyFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(embassyFile);
                List<String> townsList = config.getStringList("towns");
                townsList.add("settlement");
                townsList.add("hamlet");
                townsList.add("village");
                config.set("towns", townsList);
                List<String> preReqs = config.getStringList("pre-reqs");
                if (!preReqs.isEmpty() && preReqs.get(0).startsWith("member=")) {
                    preReqs.remove(0);
                    preReqs.add("member=settlement:hamlet:village:town:city:metropolis");
                }
                config.set("pre-reqs", preReqs);
                config.save(embassyFile);
            } catch (Exception e) {

            }
        }
        // TODO update council room, town hall, city hall, and capitol

    }
    private static void addJammerTrap(File itemTypesFolder) {
        File offenseFolder = new File(itemTypesFolder, "offense");
        if (!offenseFolder.exists()) {
            return;
        }
        File blindnessTrapFile = new File(offenseFolder, "blindness_trap.yml");
        fixPotionForFile(blindnessTrapFile);

        File jammerFile = new File(offenseFolder, "jammer_trap.yml");
        if (jammerFile.exists()) {
            return;
        }
        try {
            jammerFile.createNewFile();
            FileConfiguration config = new YamlConfiguration();
            config.set("type", "region");
            config.set("icon", "SOUL_SAND");
            config.set("name", "Jammer_Trap");
            config.set("max", 1);
            config.set("price", 400);
            ArrayList<String> groups = new ArrayList<>();
            groups.add("offense");
            config.set("groups", groups);
            config.set("level", 1);
            ArrayList<String> buildReqs = new ArrayList<>();
            buildReqs.add("TNT*4");
            buildReqs.add("OBSIDIAN*2");
            buildReqs.add("g:fence*12");
            config.set("build-reqs", buildReqs);
            config.set("build-radius", 3);
            ArrayList<String> effects = new ArrayList<>();
            effects.add("block_break");
            effects.add("block_build");
            effects.add("jammer:100.30.30");
            effects.add("temporary:1800");
            effects.add("port:member");
            config.set("effects", effects);
            config.save(jammerFile);
        } catch (Exception e) {

        }
    }

    private static void fixPotionForFile(File file) {
        if (file.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(file);
                List<String> effects = config.getStringList("effects");
                fixPotionDuration(effects);
                config.set("effects", effects);
                config.save(file);
            } catch (Exception e) {

            }
        }
    }

    private static void updateDefenses(File itemTypesFolder) {
        File defenseFolder = new File(itemTypesFolder, "defense");
        if (!defenseFolder.exists()) {
            return;
        }
        File idolFile = new File(defenseFolder, "idol.yml");
        fixPotionForFile(idolFile);
        File monumentFile = new File(defenseFolder, "monument.yml");
        fixPotionForFile(monumentFile);
        File statueFile = new File(defenseFolder, "statue.yml");
        fixPotionForFile(statueFile);
        File hospitalFile = new File(defenseFolder, "hospital.yml");
        fixPotionForFile(hospitalFile);
    }
    private static void upgradeTowns(File itemTypesFolder) {
        File townsFolder = new File(itemTypesFolder, "towns");
        if (!townsFolder.exists()) {
            return;
        }
        File settlementFile = new File(townsFolder, "settlement.yml");
        if (settlementFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(settlementFile);
                List<String> limitList =  config.getStringList("limits");
                int embassyLimit = removeEmbassy(limitList);
                if (embassyLimit > 1) {
                    limitList.add("embassy:" + embassyLimit);
                } else {
                    limitList.add("embassy:1");
                }
                limitList.add("jammer_trap:0");
                config.set("limits", limitList);
                config.save(settlementFile);
            } catch (Exception e) {

            }
        }

        File hamletFile = new File(townsFolder, "hamlet.yml");
        if (hamletFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(hamletFile);
                List<String> limitList =  config.getStringList("limits");
                int embassyLimit = removeEmbassy(limitList);
                if (embassyLimit > 2) {
                    limitList.add("embassy:" + embassyLimit);
                } else {
                    limitList.add("embassy:2");
                }
                limitList.add("jammer_trap:0");
                config.set("limits", limitList);
                config.save(hamletFile);
            } catch (Exception e) {

            }
        }

        File villageFile = new File(townsFolder, "village.yml");
        if (villageFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(villageFile);
                List<String> limitList =  config.getStringList("limits");
                int embassyLimit = removeEmbassy(limitList);
                if (embassyLimit > 3) {
                    limitList.add("embassy:" + embassyLimit);
                } else {
                    limitList.add("embassy:3");
                }
                limitList.add("jammer_trap:0");
                config.set("limits", limitList);
                config.save(villageFile);
            } catch (Exception e) {

            }
        }

        File townFile = new File(townsFolder, "town.yml");
        if (townFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(townFile);
                List<String> limitList =  config.getStringList("limits");
                int embassyLimit = removeEmbassy(limitList);
                if (embassyLimit > 5) {
                    limitList.add("embassy:" + embassyLimit);
                } else {
                    limitList.add("embassy:5");
                }
                limitList.add("jammer_trap:0");
                config.set("limits", limitList);
                config.save(townFile);
            } catch (Exception e) {

            }
        }

        File cityFile = new File(townsFolder, "city.yml");
        if (cityFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(cityFile);
                List<String> limitList =  config.getStringList("limits");
                int embassyLimit = removeEmbassy(limitList);
                if (embassyLimit > 8) {
                    limitList.add("embassy:" + embassyLimit);
                } else {
                    limitList.add("embassy:8");
                }
                limitList.add("jammer_trap:0");
                config.set("limits", limitList);
                config.save(cityFile);
            } catch (Exception e) {

            }
        }

        File metropolisFile = new File(townsFolder, "metropolis.yml");
        if (metropolisFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(metropolisFile);
                List<String> limitList =  config.getStringList("limits");
                limitList.add("jammer_trap:0");
                config.set("limits", limitList);
                config.save(metropolisFile);
            } catch (Exception e) {

            }
        }
        File miningColonyFile = new File(townsFolder, "mining-colony.yml");
        fixPotionForFile(miningColonyFile);
        File keepFile = new File(townsFolder, "keep.yml");
        fixPotionForFile(keepFile);

    }

    static void fixPotionDuration(List<String> effectList) {
        for (String effect : new HashSet<>(effectList)) {
            if (effect.startsWith("potion:")) {
                effectList.remove(effect);
                String potionString = "potion:";
                String[] potionSplit = effect.split(":")[1].split(",");
                for (String durationString : potionSplit) {
                    if (!"potion:".equals(potionString)) {
                        potionString += ",";
                    }
                    String[] currentSplit = durationString.split("\\.");
                    int duration = Integer.parseInt(currentSplit[1]);
                    duration = duration / 20;
                    currentSplit[1] = "" + duration;
                    for (String thisSplit : currentSplit) {
                        potionString += thisSplit + ".";
                    }
                    potionString = potionString.substring(0, potionString.length() - 1);
                }
                effectList.add(potionString);
            }
        }
    }
    private static int removeEmbassy(List<String> limitList) {
        for (String limit : new ArrayList<>(limitList)) {
            if (limit.contains("embassy")) {
                int limitCount = Integer.parseInt(limit.split(":")[1]);
                limitList.remove(limit);
                return limitCount;
            }
        }
        return -1;
    }

    private static void updateConfig() {
        File configFile = new File(Civs.getInstance().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return;
        }
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(configFile);
            config.set("town-rings-crumble-to-gravel", true);
            config.set("allow-teleporting-out-of-hostile-towns", true);
            config.set("allow-offline-raiding", true);
            config.set("enter-exit-messages-use-titles", true);
            config.set("always-drop-money-if-no-balance", false);
            config.save(configFile);
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }
}
