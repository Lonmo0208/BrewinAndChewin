package umpaz.brewinandchewin.neoforge.client;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;
import umpaz.brewinandchewin.neoforge.client.integration.IntoxicationAppleSkinCompatNeoForge;

@EventBusSubscriber(modid = BrewinAndChewin.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class TipsyEffects {
    private static final Logger LOGGER = LogManager.getLogger("BrewinAndChewin/TipsyEffects");

    @SubscribeEvent
    public static void whatsYourName(RenderNameTagEvent event) {
        // 名称标签渲染逻辑（当前禁用）
        // Component newName = BnCClientTextUtils.nameTagRenderer(event.getContent());
        // if (event.getContent() != newName)
        //     event.setContent(newName);
    }

    @SubscribeEvent
    public static void iCanHear(ClientChatReceivedEvent.Player event) {
        try {
            Component modifiedMessage = getChatMessage(event.getMessage());
            BnCClientTextUtils.setupChatMessage(event.getPlayerChatMessage().withUnsignedContent(modifiedMessage));
            
            PlayerChatMessage tipsyMessage = BnCClientTextUtils.getTipsyMessage();
            if (tipsyMessage != null && event.getBoundChatType() != null) {
                BnCClientTextUtils.clearTipsyMessage();

                MutableComponent boundChat = BnCClientTextUtils.getStyledChatPrefix(
                    event.getBoundChatType(), 
                    event.getBoundChatType().decorate(Component.literal("")).copy()
                );
                
                MutableComponent newMessage = tipsyMessage.decoratedContent().copy()
                    .withStyle(event.getBoundChatType().chatType().value().chat().style());
                
                event.setMessage(boundChat.append(newMessage));
            }

            BnCClientTextUtils.clearTipsyMessage();
            if (BnCClientTextUtils.clearDelayAmount <= 0) {
                BnCClientTextUtils.tipsyMessageLevel = 0;
                BnCClientTextUtils.randomSeed = 0L;
                BnCClientTextUtils.generatedRandom = false;
            } else {
                BnCClientTextUtils.clearDelayAmount--;
            }
        } catch (Exception ex) {
            LOGGER.error("Error processing chat message: {}", event.getMessage().getString(), ex);
        }
    }

    private static Component getChatMessage(Component component) {
        if (component.getContents() instanceof TranslatableContents translatable) {
            Object[] args = translatable.getArgs();
            if (args.length > 1) {
                if (args[1] instanceof Component subComponent) {
                    return subComponent;
                } else {
                    LOGGER.warn("[Brewin] Unexpected translatable arg type: {} | Key: {}", 
                               args[1].getClass().getName(), 
                               translatable.getKey());
                }
            } else {
                LOGGER.warn("[Brewin] Insufficient translatable args ({}): {}", 
                           args.length, translatable.getKey());
            }
        } else if (!(component.getContents() instanceof PlainTextContents)) {
            LOGGER.debug("[Brewin] Received non-translatable message: {} ({})", 
                        component.getString(), 
                        component.getContents().getClass().getName());
        }
        return component;
    }

    static {
        if (ModList.get().isLoaded("appleskin")) {
            LOGGER.info("Initializing AppleSkin compatibility");
            IntoxicationAppleSkinCompatNeoForge.init();
        }
    }
}
