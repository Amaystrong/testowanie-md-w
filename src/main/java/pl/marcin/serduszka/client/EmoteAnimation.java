package pl.marcin.serduszka.client;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Keyframe animation loaded from assets/serduszka/emotes/love.json.
 * Every keyframe stores, for each body part, a rotation in degrees and a position offset in model pixels.
 * "root" moves/rotates the whole player (pivot at hip height).
 */
public final class EmoteAnimation {
    public static final String[] PARTS = {"root", "head", "body", "rightArm", "leftArm", "rightLeg", "leftLeg"};
    private static final float[][] DEFAULT_POS = {
            {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {-5, 2, 0}, {5, 2, 0}, {-1.9f, 12, 0}, {1.9f, 12, 0}};

    private static EmoteAnimation instance;

    private final float length;
    private final float rootPivot;
    private final float[] times;
    /** [keyframe][part][0..5] = rotX, rotY, rotZ, posX, posY, posZ */
    private final float[][][] frames;

    private EmoteAnimation(float length, float rootPivot, float[] times, float[][][] frames) {
        this.length = length;
        this.rootPivot = rootPivot;
        this.times = times;
        this.frames = frames;
    }

    public static EmoteAnimation get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static EmoteAnimation load() {
        try (InputStream in = EmoteAnimation.class.getResourceAsStream("/assets/serduszka/emotes/love.json")) {
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            float length = root.get("length").getAsFloat();
            float pivot = root.has("rootPivotY") ? root.get("rootPivotY").getAsFloat() : 12f;
            JsonArray kfs = root.getAsJsonArray("keyframes");
            List<float[][]> frames = new ArrayList<>();
            float[] times = new float[kfs.size()];
            for (int i = 0; i < kfs.size(); i++) {
                JsonObject kf = kfs.get(i).getAsJsonObject();
                times[i] = kf.get("t").getAsFloat();
                float[][] parts = new float[PARTS.length][6];
                for (int p = 0; p < PARTS.length; p++) {
                    if (!kf.has(PARTS[p])) continue;
                    JsonObject part = kf.getAsJsonObject(PARTS[p]);
                    read(part, "rot", parts[p], 0);
                    read(part, "pos", parts[p], 3);
                }
                frames.add(parts);
            }
            return new EmoteAnimation(length, pivot, times, frames.toArray(new float[0][][]));
        } catch (Exception e) {
            throw new RuntimeException("Serduszka: failed to load emote animation", e);
        }
    }

    private static void read(JsonObject part, String key, float[] out, int offset) {
        if (!part.has(key)) return;
        JsonArray a = part.getAsJsonArray(key);
        for (int i = 0; i < 3; i++) out[offset + i] = a.get(i).getAsFloat();
    }

    /** Interpolated pose at time t seconds (loops). */
    public float[][] sample(float t) {
        t = t % length;
        if (t < 0) t += length;
        int n = times.length;
        int i = n - 1;
        for (int k = 0; k < n - 1; k++) {
            if (t < times[k + 1]) {
                i = k;
                break;
            }
        }
        int j = (i + 1) % n;
        float t0 = times[i];
        float t1 = (i + 1 < n) ? times[i + 1] : length;
        float f = (t1 - t0) <= 0 ? 0 : (t - t0) / (t1 - t0);
        f = 0.5f - 0.5f * Mth.cos((float) Math.PI * Mth.clamp(f, 0, 1));
        float[][] a = frames[i], b = frames[j];
        float[][] out = new float[PARTS.length][6];
        for (int p = 0; p < PARTS.length; p++) {
            for (int c = 0; c < 6; c++) {
                out[p][c] = Mth.lerp(f, a[p][c], b[p][c]);
            }
        }
        return out;
    }

    /** Applies limb poses to the model (called at the end of HumanoidModel#setupAnim). */
    public static void applyToModel(HumanoidModel<?> model, float[][] pose) {
        set(model.head, pose[1], DEFAULT_POS[1]);
        set(model.body, pose[2], DEFAULT_POS[2]);
        set(model.rightArm, pose[3], DEFAULT_POS[3]);
        set(model.leftArm, pose[4], DEFAULT_POS[4]);
        set(model.rightLeg, pose[5], DEFAULT_POS[5]);
        set(model.leftLeg, pose[6], DEFAULT_POS[6]);
        model.hat.copyFrom(model.head);
    }

    /** Restores the parts that vanilla setupAnim does not always reset. */
    public static void resetModel(HumanoidModel<?> model) {
        ModelPart[] parts = {model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg};
        for (int p = 0; p < parts.length; p++) {
            float[] d = DEFAULT_POS[p + 1];
            parts[p].setPos(d[0], d[1], d[2]);
            parts[p].setRotation(0, 0, 0);
        }
        model.hat.copyFrom(model.head);
    }

    private static void set(ModelPart part, float[] v, float[] def) {
        part.setRotation(v[0] * Mth.DEG_TO_RAD, v[1] * Mth.DEG_TO_RAD, v[2] * Mth.DEG_TO_RAD);
        part.setPos(def[0] + v[3], def[1] + v[4], def[2] + v[5]);
    }

    /** Whole-body transform, applied around the player before the renderer's own transforms. */
    public void applyRoot(PoseStack stack, float[][] pose, float bodyYaw) {
        float[] r = pose[0];
        float facing = 180.0F - bodyYaw;
        stack.mulPose(Axis.YP.rotationDegrees(facing));
        stack.translate(r[3] / 16f, r[4] / 16f, r[5] / 16f);
        stack.translate(0, rootPivot / 16f, 0);
        stack.mulPose(Axis.ZP.rotationDegrees(r[2]));
        stack.mulPose(Axis.YP.rotationDegrees(r[1]));
        stack.mulPose(Axis.XP.rotationDegrees(r[0]));
        stack.translate(0, -rootPivot / 16f, 0);
        stack.mulPose(Axis.YP.rotationDegrees(-facing));
    }
}
