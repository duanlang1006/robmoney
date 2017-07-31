package com.mit.money.card.parser;

import java.lang.reflect.Constructor;

/**
 * Created by hxd on 16-1-21.
 */
public class CardParserFactory {
    public static CardParser createCardParser(String parserName, Object tag) {
        CardParser parser = null;
        try {
            Class cls = Class.forName(CardParserFactory.class.getPackage().getName() + "." + parserName);
            Constructor ct = cls.getConstructor(Object.class);
            parser = (CardParser) ct.newInstance(tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parser;
    }
}
