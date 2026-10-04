package com.erik.redstonedust.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

/**
 * The Redstone Tutorial Book.
 *
 * <p>Right-click it and Eric receives a real, readable written book whose pages
 * explain — in simple Bulgarian — how to build the lever -> redstone dust ->
 * lamp circuit from scratch. The tutorial item itself is reusable.
 */
public class TutorialBookItem extends Item {

	private static final String TITLE = "Редстоун за начинаещи";
	private static final String AUTHOR = "Ерик";

	public TutorialBookItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide()) {
			ItemStack book = createWrittenBook();
			if (!player.addItem(book)) {
				player.drop(book, false);
			}
			level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN,
					SoundSource.PLAYERS, 1.0f, 1.0f);
			player.displayClientMessage(
					Component.translatable("message.redstonedust.book_given")
							.withStyle(ChatFormatting.GOLD),
					true);
		}
		return InteractionResult.SUCCESS;
	}

	/** Builds the vanilla written book carrying the Bulgarian step-by-step pages. */
	private static ItemStack createWrittenBook() {
		List<Filterable<Component>> pages = new ArrayList<>();

		pages.add(page(
				"Здравей, Ерик!\n\n"
						+ "Тази книжка те учи да направиш твоята първа\n"
						+ "редстоун машинка:\n\n"
						+ "ЛОСТ -> РЕДСТОУН ПРАХ -> ЛАМПА.\n\n"
						+ "Обърни страницата ->"));

		pages.add(page(
				"Стъпка 1.\n\n"
						+ "Сложи 4 каменни блока в редичка на земята.\n\n"
						+ "Това е пътечката, по която ще тече тока."));

		pages.add(page(
				"Стъпка 2.\n\n"
						+ "Върху ПЪРВИЯ камък сложи ЛОСТ (lever).\n\n"
						+ "Лостът е като ключ за лампата."));

		pages.add(page(
				"Стъпка 3.\n\n"
						+ "Върху средните два камъка прокарай\n"
						+ "РЕДСТОУН ПРАХ (redstone dust).\n\n"
						+ "Прахът свети червено - това е жицата!"));

		pages.add(page(
				"Стъпка 4.\n\n"
						+ "Върху последния камък сложи\n"
						+ "РЕДСТОУН ЛАМПА (redstone lamp)."));

		pages.add(page(
				"Стъпка 5.\n\n"
						+ "Дръпни ЛОСТА!\n\n"
						+ "Токът тръгва по праха и...\n\n"
						+ "ЛАМПАТА СВЕТВА! :)\n\n"
						+ "Дръпни пак - и тя угасва."));

		pages.add(page(
				"Браво, Ерик!\n\n"
						+ "Това е първата ти схема.\n\n"
						+ "Съвет: ако нямаш блокове, използвай\n"
						+ "Комплекта за игра с редстоун - той\n"
						+ "сглобява всичко вместо теб!"));

		WrittenBookContent content = new WrittenBookContent(
				Filterable.passThrough(TITLE),
				AUTHOR,
				0,
				pages,
				true);

		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
		book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
		return book;
	}

	private static Filterable<Component> page(String text) {
		return Filterable.passThrough(Component.literal(text));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext tooltipContext,
			TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		consumer.accept(Component.translatable("tooltip.redstonedust.tutorial_book.line1")
				.withStyle(ChatFormatting.GRAY));
		consumer.accept(Component.translatable("tooltip.redstonedust.tutorial_book.line2")
				.withStyle(ChatFormatting.DARK_GRAY));
	}
}
