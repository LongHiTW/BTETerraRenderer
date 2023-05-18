package com.mndk.bteterrarenderer.gui.components;

import com.mndk.bteterrarenderer.connector.gui.FontConnector;
import com.mndk.bteterrarenderer.connector.gui.GuiStaticConnector;
import com.mndk.bteterrarenderer.connector.minecraft.GameInputConnector;
import com.mndk.bteterrarenderer.connector.minecraft.InputKey;
import com.mndk.bteterrarenderer.util.BtrUtil;
import com.mndk.bteterrarenderer.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Predicate;

/**
 * Copied from both 1.12.2's <code>net.minecraft.client.gui.GuiTextField</code>
 * and 1.18.2's <code>net.minecraft.client.gui.components.EditBox</code>
 */
public class GuiTextFieldImpl extends GuiAbstractWidgetImpl {

    private static final int BORDER_COLOR_FOCUSED = 0xFFFFFFFF;
    private static final int BORDER_COLOR = 0xFFA0A0A0;
    public static final int ENABLED_TEXT_COLOR = 0xE0E0E0;
    public static final int DISABLED_TEXT_COLOR = 0x707070;

    @Setter
    private Integer textColor;
    @Setter
    private int maxStringLength = 32;
    private int frame; // TODO use this
    private final boolean bordered = true;
    private boolean shiftPressed;
    @Getter @Setter
    private int displayPos, cursorPos, highlightPos;
    @Setter
    private Predicate<String> validator = s -> true;

    public GuiTextFieldImpl(int x, int y, int width, int height) {
        super(x, y, width, height, "");
    }

    public void setText(String text) {
        if (this.validator.test(text)) {
            this.text = text.length() > maxStringLength ? text.substring(0, maxStringLength) : text;

            this.moveCursorToEnd();
            this.setHighlightPos(this.cursorPos);
        }
    }

    public String getHighlighted() {
        int start = Math.min(this.cursorPos, this.highlightPos);
        int end = Math.max(this.cursorPos, this.highlightPos);
        return this.text.substring(start, end);
    }

    public void insertText(String text) {
        int start = Math.min(this.cursorPos, this.highlightPos);
        int end = Math.max(this.cursorPos, this.highlightPos);
        int max = this.maxStringLength - this.text.length() - (start - end);
        String filtered = StringUtil.filterMinecraftAllowedCharacters(text);
        int length = filtered.length();
        if (max < length) {
            filtered = filtered.substring(0, max);
            length = max;
        }

        String result = (new StringBuilder(this.text)).replace(start, end, filtered).toString();
        if (this.validator.test(result)) {
            this.text = result;
            this.setCursorPosition(start + length);
            this.setHighlightPos(this.cursorPos);
        }
    }

    private void deleteText(int delta) {
        if (GameInputConnector.INSTANCE.isControlKeyDown()) {
            this.deleteWords(delta);
        } else {
            this.deleteChars(delta);
        }

    }

    public void deleteWords(int delta) {
        if (!this.text.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(delta) - this.cursorPos);
            }
        }
    }

    public void deleteChars(int delta) {
        if (!this.text.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int i = this.getCursorPos(delta);
                int j = Math.min(i, this.cursorPos);
                int k = Math.max(i, this.cursorPos);
                if (j != k) {
                    String s = (new StringBuilder(this.text)).delete(j, k).toString();
                    if (this.validator.test(s)) {
                        this.text = s;
                        this.moveCursorTo(j);
                    }
                }
            }
        }
    }

    public int getWordPosition(int delta) {
        return this.getWordPosition(delta, this.getCursorPos());
    }

    private int getWordPosition(int delta, int cursorPos) {
        return this.getWordPosition(delta, cursorPos, true);
    }

    private int getWordPosition(int delta, int cursorPos, boolean p_94143_) {
        int i = cursorPos;
        boolean flag = delta < 0;
        int j = Math.abs(delta);

        for(int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while(p_94143_ && i < l && this.text.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while(p_94143_ && i > 0 && this.text.charAt(i - 1) == ' ') {
                    --i;
                }

                while(i > 0 && this.text.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursor(int delta) {
        this.moveCursorTo(this.getCursorPos(delta));
    }

    private int getCursorPos(int delta) {
        return StringUtil.offsetByCodepoints(this.text, this.cursorPos, delta);
    }

    public void moveCursorTo(int p_94193_) {
        this.setCursorPosition(p_94193_);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }
    }

    public void setCursorPosition(int p_94197_) {
        this.cursorPos = BtrUtil.clamp(p_94197_, 0, this.text.length());
    }

    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    public void moveCursorToEnd() {
        this.moveCursorTo(this.text.length());
    }

    public boolean keyPressed(InputKey key) {
        GameInputConnector inputConnector = GameInputConnector.INSTANCE;

        if (!this.canConsumeInput()) {
            return false;
        } else {
            this.shiftPressed = inputConnector.isShiftKeyDown();
            if (inputConnector.isKeySelectAll(key)) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                return true;
            } else if (inputConnector.isKeyCopy(key)) {
                inputConnector.setClipboardContent(this.getHighlighted());
                return true;
            } else if (inputConnector.isKeyPaste(key)) {
                if (this.enabled) {
                    this.insertText(inputConnector.getClipboardContent());
                }

                return true;
            } else if (inputConnector.isKeyCut(key)) {
                inputConnector.setClipboardContent(this.getHighlighted());
                if (this.enabled) {
                    this.insertText("");
                }

                return true;
            } else {
                switch(key) {
                    case KEY_BACKSPACE:
                        if (this.enabled) {
                            this.shiftPressed = false;
                            this.deleteText(-1);
                            this.shiftPressed = inputConnector.isShiftKeyDown();
                        }
                        return true;
                    case KEY_INSERT:
                    case KEY_DOWN:
                    case KEY_UP:
                    case KEY_PAGE_UP:
                    case KEY_PAGE_DOWN:
                    default:
                        return false;
                    case KEY_DELETE:
                        if (this.enabled) {
                            this.shiftPressed = false;
                            this.deleteText(1);
                            this.shiftPressed = inputConnector.isShiftKeyDown();
                        }
                        return true;
                    case KEY_RIGHT:
                        if (inputConnector.isControlKeyDown()) {
                            this.moveCursorTo(this.getWordPosition(1));
                        } else {
                            this.moveCursor(1);
                        }
                        return true;
                    case KEY_LEFT:
                        if (inputConnector.isControlKeyDown()) {
                            this.moveCursorTo(this.getWordPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }
                        return true;
                    case KEY_HOME:
                        this.moveCursorToStart();
                        return true;
                    case KEY_END:
                        this.moveCursorToEnd();
                        return true;
                }
            }
        }
    }

    public boolean canConsumeInput() {
        return this.isVisible() && this.isFocused() && this.enabled;
    }

    public boolean keyTyped(char typedChar, int mods) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (StringUtil.isMinecraftAllowedCharacter(typedChar)) {
            if (this.enabled) {
                this.insertText(Character.toString(typedChar));
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean mousePressed(double mouseX, double mouseY, int mouseButton) {
        if (!this.isVisible())
            return false;

        boolean mouseOnWidget = this.isMouseOnWidget(mouseX, mouseY);
        this.setFocused(mouseOnWidget);

        if (!mouseOnWidget && mouseButton == 0)
            return false;

        int i = ((int) mouseX) - this.x;
        if (this.bordered) {
            i -= 4;
        }

        FontConnector font = FontConnector.INSTANCE;
        String s = font.trimStringToWidth(this.text.substring(this.displayPos), this.getInnerWidth());
        this.moveCursorTo(font.trimStringToWidth(s, i).length() + this.displayPos);
        return true;
    }

    public void drawComponent(double mouseX, double mouseY, float partialTicks) {
        FontConnector font = FontConnector.INSTANCE;

        if (this.isVisible()) {
            if (this.bordered) {
                int borderColor = this.isFocused() ? BORDER_COLOR_FOCUSED : BORDER_COLOR;
                GuiStaticConnector.INSTANCE.drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, borderColor);
                GuiStaticConnector.INSTANCE.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);
            }

            int i2 = this.textColor != null ? this.textColor : (this.enabled ? ENABLED_TEXT_COLOR : DISABLED_TEXT_COLOR);
            int j = this.cursorPos - this.displayPos;
            int k = this.highlightPos - this.displayPos;
            String trimmed = font.trimStringToWidth(this.text.substring(this.displayPos), this.getInnerWidth());
            boolean flag = j >= 0 && j <= trimmed.length();
            boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
            int l = this.bordered ? this.x + 4 : this.x;
            int i1 = this.bordered ? this.y + (this.height - 8) / 2 : this.y;
            int j1 = l;
            if (k > trimmed.length()) {
                k = trimmed.length();
            }

            if (!trimmed.isEmpty()) {
                String s1 = flag ? trimmed.substring(0, j) : trimmed;
                j1 = font.drawStringWithShadow(s1, (float)l, (float)i1, i2);
            }

            boolean flag2 = this.cursorPos < this.text.length() || this.text.length() >= this.maxStringLength;
            int k1 = j1;
            if (!flag) {
                k1 = j > 0 ? l + this.width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (!trimmed.isEmpty() && flag && j < trimmed.length()) {
                font.drawStringWithShadow(trimmed.substring(j), (float)j1, (float)i1, i2);
            }

            if (flag1) {
                if (flag2) {
                    GuiStaticConnector.INSTANCE.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
                } else {
                    font.drawStringWithShadow("_", (float)k1, (float)i1, i2);
                }
            }

            if (k != j) {
                int l1 = l + font.getStringWidth(trimmed.substring(0, k));
                this.drawSelectionBox(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
            }

        }
    }

    private void drawSelectionBox(int startX, int startY, int endX, int endY) {
        if (startX < endX) {
            int i = startX;
            startX = endX;
            endX = i;
        }

        if (startY < endY) {
            int j = startY;
            startY = endY;
            endY = j;
        }

        if (endX > this.x + this.width) {
            endX = this.x + this.width;
        }

        if (startX > this.x + this.width) {
            startX = this.x + this.width;
        }

        GuiStaticConnector.INSTANCE.drawTextFieldHighlight(startX, startY, endX, endY);
    }

    public int getInnerWidth() {
        return this.bordered ? this.width - 8 : this.width;
    }
}
