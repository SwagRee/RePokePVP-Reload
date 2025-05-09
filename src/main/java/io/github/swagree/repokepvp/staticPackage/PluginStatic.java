package io.github.swagree.repokepvp.staticPackage;

import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import io.github.swagree.repokepvp.entity.Member;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PluginStatic {
    public static final EnumSpecies[] UltraALL = new EnumSpecies[]{
            EnumSpecies.Buzzwole, EnumSpecies.Pheromosa, EnumSpecies.Xurkitree,
            EnumSpecies.Celesteela, EnumSpecies.Guzzlord, EnumSpecies.Kartana,
            EnumSpecies.Blacephalon, EnumSpecies.Poipole, EnumSpecies.Naganadel,
            EnumSpecies.Stakataka
    };

    public static Queue<UUID> battleQueue = new LinkedList<>();

    public static Map<UUID, Member> members = new ConcurrentHashMap<>();

}
