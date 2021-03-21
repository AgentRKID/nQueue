package io.github.agentrkid.nqueue.bukkit.commands.param;

import com.jonahseguin.drink.argument.CommandArg;
import com.jonahseguin.drink.exception.CommandExitMessage;
import com.jonahseguin.drink.parametric.DrinkProvider;
import io.github.agentrkid.nqueue.api.Queue;
import io.github.agentrkid.nqueue.bukkit.nQueueBukkit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QueueProvider extends DrinkProvider<Queue> {
    @Override
    public boolean doesConsumeArgument() {
        return true;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Nullable
    @Override
    public Queue provide(@Nonnull CommandArg arg, @Nonnull List<? extends Annotation> annotations) throws CommandExitMessage {
        String queueName = arg.get();
        Queue queue = nQueueBukkit.getInstance().getQueueManager().getQueueById(queueName);

        if (queue != null) {
            return queue;
        }
        throw new CommandExitMessage("No queue with the name '" + queueName + "' found.");
    }

    @Override
    public String argumentDescription() {
        return "Queue";
    }

    @Override
    public List<String> getSuggestions(@Nonnull String prefix) {
        return new ArrayList<>(nQueueBukkit.getInstance().getQueueManager().getQueues().stream().map(Queue::getId).collect(Collectors.toList()));
    }
}
