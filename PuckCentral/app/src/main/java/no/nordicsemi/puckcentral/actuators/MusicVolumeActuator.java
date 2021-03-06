package no.nordicsemi.puckcentral.actuators;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;

import org.droidparts.activity.Activity;
import org.droidparts.annotation.inject.InjectSystemService;
import org.json.JSONException;
import org.json.JSONObject;

import no.nordicsemi.puckcentral.R;
import no.nordicsemi.puckcentral.models.Action;
import no.nordicsemi.puckcentral.models.Rule;

public class MusicVolumeActuator extends Actuator {

    public static final String[] VOLUMES = new String[] {"silent", "maximum"};
    public static final String ARGUMENT_AMOUNT = "amount";

    @InjectSystemService
    AudioManager mAudioManager;

    @Override
    public String describeActuator() {
        return "Music Volume Actuator";
    }

    @Override
    public String describeArguments(JSONObject arguments) {
        try {
            return "Sets music volume level to " + arguments.get(ARGUMENT_AMOUNT);
        } catch (JSONException e) {
            e.printStackTrace();
            return "Invalid arguments for actuator";
        }
    }

    @Override
    public int getId() {
        return 3402;
    }

    @Override
    void actuate(JSONObject arguments) throws JSONException {
        int amount = (int) arguments.get(ARGUMENT_AMOUNT);
        amount = Math.min(amount, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, amount, 0);
    }

    @Override
    public AlertDialog getActuatorDialog(Activity activity, final Action action, final Rule rule, final ActuatorDialogFinishListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(describeActuator());
        builder.setItems(VOLUMES, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                int amount;
                switch (which) {
                    case 0: amount = 0; break;
                    case 1: amount = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); break;
                    default: throw new IllegalStateException();
                }

                String arguments = Action.jsonStringBuilder(ARGUMENT_AMOUNT, amount);
                action.setArguments(arguments);
                rule.addAction(action);
                listener.onActuatorDialogFinish(action, rule);
            }
        });
        builder.setNegativeButton(activity.getString(R.string.abort), null);
        return builder.create();
    }
}
