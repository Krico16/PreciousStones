package net.sacredlabyrinth.Phaed.PreciousStones.entries;

import net.sacredlabyrinth.Phaed.PreciousStones.*;
import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;


public class FieldSign
{
    private Sign sign;
    private boolean valid = true;
    private String tag;
    private Field field;
    private boolean fieldSign;
    private String period;
    private int price;
    private BlockTypeEntry item;
    private String playerName;
    private String failReason;

    public FieldSign(Block signBlock)
    {
        if (!SignHelper.isSign(signBlock))
        {
            valid = false;
            return;
        }

        sign = ((Sign) signBlock.getState());

        String[] lines = sign.getLines();

        valid = extractData(signBlock, lines);
    }

    public FieldSign(Block signBlock, String[] lines, Player player)
    {
        if (!SignHelper.isSign(signBlock))
        {
            valid = false;
            return;
        }

        valid = extractData(signBlock, lines);
        playerName = player.getName();
    }

    public boolean extractData(Block signBlock, String[] lines)
    {
        tag = ChatColor.stripColor(lines[0]);

        price = SignHelper.extractPrice(ChatColor.stripColor(lines[1]));

        if (price == 0)
        {
            failReason = "fieldSignNoPrice";
            return false;
        }

        item = SignHelper.extractItemFromParenthesis(ChatColor.stripColor(lines[1]));

        if (!isBuyable())
        {
            period = ChatColor.stripColor(lines[2]);

            if (!SignHelper.isValidPeriod(period))
            {
                failReason = "fieldSignInvalidPeriod";
                return false;
            }
        }

        fieldSign = tag.equalsIgnoreCase(ChatBlock.format("fieldSignRent")) || tag.equalsIgnoreCase(ChatBlock.format("fieldSignBuy")) || tag.equalsIgnoreCase(ChatBlock.format("fieldSignShare"));

        if (!fieldSign)
        {
            failReason = "fieldSignBadTag";
            return false;
        }

        if (item == null)
        {
            if (!PreciousStones.getInstance().getPermissionsManager().hasEconomy())
            {
                failReason = "fieldSignNoEco";
                return false;
            }
        }

        Block attachedBlock = SignHelper.getAttachedBlock(signBlock);
        field = PreciousStones.getInstance().getForceFieldManager().getField(attachedBlock);

        if (field == null)
        {
            failReason = "fieldSignMustBeOnField";
            return false;
        }

        if (playerName != null)
        {
            if (!field.isOwner(playerName))
            {
                failReason = "fieldSignNotOwner";
                return false;
            }
        }

        if (isRentable())
        {
            if (!field.hasFlag(FieldFlag.RENTABLE))
            {
                failReason = "fieldSignNotRentable";
                return false;
            }
        }

        if (isShareable())
        {
            if (!field.hasFlag(FieldFlag.SHAREABLE))
            {
                failReason = "fieldSignNotShareable";
                return false;
            }
        }

        if (isBuyable())
        {
            if (!field.hasFlag(FieldFlag.BUYABLE))
            {
                failReason = "fieldSignNotBuyable";
                return false;
            }
        }

        return true;
    }

    public boolean isValid()
    {
        return valid;
    }

    public boolean isRentable()
    {
        return tag.equalsIgnoreCase(ChatBlock.format("fieldSignRent"));
    }

    public boolean isBuyable()
    {
        return tag.equalsIgnoreCase(ChatBlock.format("fieldSignBuy"));
    }

    public boolean isShareable()
    {
        return tag.equalsIgnoreCase(ChatBlock.format("fieldSignShare"));
    }

    public void setRentedColor()
    {
        sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(sign.getLine(0)) + ChatColor.RED + "" + ChatColor.BOLD);
        sign.update();
    }

    public void setBoughtColor(Player player)
    {
        sign.setLine(0, ChatColor.RED + "" + ChatColor.BOLD + ChatColor.stripColor(sign.getLine(0)) + ChatColor.RED + "" + ChatColor.BOLD);
        sign.setLine(3, ChatColor.BOLD + player.getName() + ChatColor.BOLD);
        sign.update();
    }

    public void setSharedColor()
    {
        sign.setLine(0, ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.stripColor(sign.getLine(0)) + ChatColor.RED + "" + ChatColor.BOLD);
        sign.update();
    }

    public void setAvailableColor()
    {
        sign.setLine(0, ChatColor.BOLD + ChatColor.stripColor(sign.getLine(0)) + ChatColor.BOLD);
        sign.setLine(3, "");
        sign.update();
    }

    public void updateRemainingTime(int seconds)
    {
        sign.setLine(3, ChatColor.BOLD + SignHelper.secondsToPeriods(seconds) + ChatColor.BOLD);
        sign.update();
    }

    public void cleanRemainingTime()
    {
        sign.setLine(3, "");
        sign.update();
    }

    /**
     * Throws the fieldSign back at the player
     */
    public void eject()
    {
        Helper.dropBlockWipe(sign.getBlock());
    }

    /**
     * Returns the block the fieldSign is attacked to
     *
     * @return
     */
    public Block getAttachedBlock()
    {
        return SignHelper.getAttachedBlock(sign.getBlock());
    }

    public Field getField()
    {
        if (field != null)
        {
            return field;
        }

        return PreciousStones.getInstance().getForceFieldManager().getField(getAttachedBlock());
    }

    public boolean isFieldSign()
    {
        return fieldSign;
    }

    public String getPeriod()
    {
        return period;
    }

    public int getPrice()
    {
        return price;
    }

    public BlockTypeEntry getItem()
    {
        return item;
    }

    public Sign getSign()
    {
        return sign;
    }

    public String getFailReason()
    {
        return failReason;
    }

    public void setFailReason(String failReason)
    {
        this.failReason = failReason;
    }
}