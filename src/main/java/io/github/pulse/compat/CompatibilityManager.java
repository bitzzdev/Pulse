package io.github.pulse.compat;

import io.github.pulse.api.ModuleType;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompatibilityManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("pulse-compat");

    private static final Map<String, String> KNOWN_MODS = new LinkedHashMap<>();
    private final Map<String, ModInfo> detectedMods = new LinkedHashMap<>();
    private final Set<ModuleType> disabledModules = new HashSet<>();
    private final Set<ModuleType> reducedModules = new HashSet<>();
    private final List<String> conflictMessages = new ArrayList<>();
    private boolean initialized = false;

    static {
        KNOWN_MODS.put("sodium", "Sodium");
        KNOWN_MODS.put("immediatelyfast", "ImmediatelyFast");
        KNOWN_MODS.put("entityculling", "EntityCulling");
        KNOWN_MODS.put("ferritecore", "FerriteCore");
        KNOWN_MODS.put("lithium", "Lithium");
        KNOWN_MODS.put("noisium", "Noisium");
        KNOWN_MODS.put("iris", "Iris");
        KNOWN_MODS.put("modernfix", "ModernFix");
        KNOWN_MODS.put("c2me", "C2ME");
        KNOWN_MODS.put("forgetmechunk", "ForgelMeChunk");
        KNOWN_MODS.put("dynamicfps", "Dynamic FPS");
        KNOWN_MODS.put("enhancedblockentities", "Enhanced Block Entities");
        KNOWN_MODS.put("particlesenhanced", "Particles Enhanced");
    }

    public void initialize() {
        detectedMods.clear();
        disabledModules.clear();
        reducedModules.clear();
        conflictMessages.clear();

        for (Map.Entry<String, String> entry : KNOWN_MODS.entrySet()) {
            String modId = entry.getKey();
            String modName = entry.getValue();
            boolean loaded = FabricLoader.getInstance().isModLoaded(modId);
            detectedMods.put(modId, new ModInfo(modId, modName, loaded));

            if (loaded) {
                LOGGER.info("Detected compatible mod: {} ({})", modName, modId);
            }
        }

        checkSodium();
        checkImmediatelyFast();
        checkEntityCulling();
        checkIris();
        checkOtherOptimizationMods();

        initialized = true;
        LOGGER.info("Compatibility scan complete: {} mods detected, {} modules disabled, {} modules reduced",
            detectedMods.values().stream().filter(ModInfo::isLoaded).count(),
            disabledModules.size(), reducedModules.size());
    }

    private void checkSodium() {
        if (isModLoaded("sodium")) {
            LOGGER.info("Sodium detected: disabling shadow and cloud optimizers (Sodium handles these)");
            disabledModules.add(ModuleType.SHADOWS);
            disabledModules.add(ModuleType.CLOUDS);
            conflictMessages.add("Sodium: ShadowOptimizer disabled (Sodium provides shadow optimizations)");
            conflictMessages.add("Sodium: CloudOptimizer disabled (Sodium provides cloud optimizations)");
        }
    }

    private void checkImmediatelyFast() {
        if (isModLoaded("immediatelyfast")) {
            LOGGER.info("ImmediatelyFast detected: reducing UI optimizer intensity");
            reducedModules.add(ModuleType.UI);
            conflictMessages.add("ImmediatelyFast: UIOptimizer partially disabled (ImmediatelyFast handles UI batching)");
        }
    }

    private void checkEntityCulling() {
        if (isModLoaded("entityculling")) {
            LOGGER.info("EntityCulling detected: reducing entity optimizer intensity");
            reducedModules.add(ModuleType.ENTITIES);
            conflictMessages.add("EntityCulling: EntityOptimizer intensity reduced (EntityCulling handles culling)");
        }
    }

    private void checkIris() {
        if (isModLoaded("iris")) {
            LOGGER.info("Iris detected: enabling full shadow integration");
            conflictMessages.add("Iris: Shadow integration enabled (Iris provides shader-based shadows)");
        }
    }

    private void checkOtherOptimizationMods() {
        String[] otherMods = {"ferritecore", "lithium", "noisium", "modernfix", "c2me",
            "forgetmechunk", "dynamicfps", "enhancedblockentities", "particlesenhanced"};

        for (String modId : otherMods) {
            if (isModLoaded(modId)) {
                String name = KNOWN_MODS.get(modId);
                LOGGER.info("{} detected: complementary optimization, no conflicts", name);
                conflictMessages.add(name + ": Complementary optimization, fully compatible");
            }
        }
    }

    public void shutdown() {
        initialized = false;
        detectedMods.clear();
        disabledModules.clear();
        reducedModules.clear();
        conflictMessages.clear();
        LOGGER.info("CompatibilityManager shut down");
    }

    public boolean hasMod(String modId) {
        return detectedMods.containsKey(modId) && detectedMods.get(modId).isLoaded();
    }

    public Map<String, ModInfo> getDetectedMods() {
        return Collections.unmodifiableMap(detectedMods);
    }

    public boolean isModuleConflicting(ModuleType type) {
        return disabledModules.contains(type) || reducedModules.contains(type);
    }

    public boolean isModuleDisabled(ModuleType type) {
        return disabledModules.contains(type);
    }

    public boolean isModuleReduced(ModuleType type) {
        return reducedModules.contains(type);
    }

    public Set<ModuleType> getDisabledModules() {
        return Collections.unmodifiableSet(disabledModules);
    }

    public Set<ModuleType> getReducedModules() {
        return Collections.unmodifiableSet(reducedModules);
    }

    public String getCompatibilityReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Pulse Compatibility Report ===\n");
        report.append("Detected Mods:\n");

        boolean anyDetected = false;
        for (ModInfo info : detectedMods.values()) {
            String status = info.isLoaded() ? "DETECTED" : "not found";
            report.append("  ").append(info.getName()).append(" (").append(info.getModId()).append("): ").append(status).append("\n");
            if (info.isLoaded()) {
                anyDetected = true;
            }
        }

        if (!anyDetected) {
            report.append("  No optimization mods detected\n");
        }

        report.append("\nCompatibility Actions:\n");
        if (conflictMessages.isEmpty()) {
            report.append("  No conflicts detected\n");
        } else {
            for (String msg : conflictMessages) {
                report.append("  * ").append(msg).append("\n");
            }
        }

        report.append("\nDisabled Modules: ");
        if (disabledModules.isEmpty()) {
            report.append("None");
        } else {
            report.append(disabledModules);
        }

        report.append("\nReduced Modules: ");
        if (reducedModules.isEmpty()) {
            report.append("None");
        } else {
            report.append(reducedModules);
        }

        return report.toString();
    }

    private boolean isModLoaded(String modId) {
        ModInfo info = detectedMods.get(modId);
        return info != null && info.isLoaded();
    }

    public static class ModInfo {
        private final String modId;
        private final String name;
        private final boolean loaded;

        public ModInfo(String modId, String name, boolean loaded) {
            this.modId = modId;
            this.name = name;
            this.loaded = loaded;
        }

        public String getModId() {
            return modId;
        }

        public String getName() {
            return name;
        }

        public boolean isLoaded() {
            return loaded;
        }
    }
}
