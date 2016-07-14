package com.eaglesakura.andriders.db.command;

import com.eaglesakura.lambda.Matcher1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandCollection {
    final List<CommandData> mCommands;

    public CommandCollection(List<CommandData> commands) {
        mCommands = commands;
    }

    /**
     * 条件にマッチするコマンドを全て取得する
     *
     * @param matcher 条件
     */
    public List<CommandData> list(Matcher1<CommandData> matcher) {
        try {
            List<CommandData> result = new ArrayList<>();
            for (CommandData data : mCommands) {
                if (matcher.match(data)) {
                    result.add(data);
                }
            }

            Collections.sort(result, (l, r) -> {
                if (l.getCategory() != r.getCategory()) {
                    return Integer.compare(l.getCategory(), r.getCategory());
                } else {
                    return l.getCommandKey().getKey().compareTo(r.getCommandKey().getKey());
                }
            });
            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
