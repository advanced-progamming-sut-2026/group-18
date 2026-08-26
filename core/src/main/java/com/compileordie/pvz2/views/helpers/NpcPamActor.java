package com.compileordie.pvz2.views.helpers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.compileordie.pvz2.models.entities.npcs.NpcType;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;

public class NpcPamActor extends Actor {

    public enum State { ENTER, TALK, EXIT, FINISHED }

    private final PamPlayer pamPlayer;
    private final NpcType npcType;
    private State currentState = State.ENTER;
    private float stateTime = 0f;

    public NpcPamActor(PamPlayer pamPlayer, NpcType npcType) {
        this.pamPlayer = pamPlayer;
        this.npcType = npcType;
        setSize(200, 200);
    }

    public void triggerExit() {
        if (currentState != State.EXIT && currentState != State.FINISHED) {
            currentState = State.EXIT;
            stateTime = 0f;
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;

        ClipRef currentClip = getCurrentClip();
        if (currentClip == null) return;

        float duration = currentClip.duration;
        if (currentState == State.ENTER && stateTime >= duration) {
            currentState = State.TALK;
            stateTime = 0f;
        } else if (currentState == State.EXIT && stateTime >= duration) {
            currentState = State.FINISHED;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (pamPlayer == null || currentState == State.FINISHED) return;

        ClipRef clip = getCurrentClip();
        if (clip != null) {
            Color oldColor = batch.getColor();
            batch.setColor(Color.WHITE); // Fixes NPC turning dark/black from background shade
            pamPlayer.draw(batch, clip, stateTime, getX() + getWidth() / 2f, getY(), false);
            batch.setColor(oldColor);
        }
    }

    private ClipRef getCurrentClip() {
        if (pamPlayer == null) return null;
        String animName = switch (currentState) {
            case ENTER -> npcType.getEnterAnim();
            case TALK -> npcType.getTalkAnim();
            case EXIT, FINISHED -> npcType.getExitAnim();
        };
        return pamPlayer.getClip(npcType.getPamPath(), animName);
    }
}
