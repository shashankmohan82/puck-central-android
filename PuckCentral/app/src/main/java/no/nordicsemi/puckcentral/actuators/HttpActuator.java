package no.nordicsemi.puckcentral.actuators;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.droidparts.activity.Activity;
import org.droidparts.annotation.inject.InjectDependency;
import org.droidparts.concurrent.task.SimpleAsyncTask;
import org.droidparts.net.http.RESTClient;
import org.json.JSONException;
import org.json.JSONObject;

import no.nordicsemi.puckcentral.R;
import no.nordicsemi.puckcentral.models.Action;
import no.nordicsemi.puckcentral.models.Rule;

public class HttpActuator extends Actuator {

    public static final String URL = "URL";
    public static final String DATA = "data";
    public static final String ENCODING = "application/x-www-form-urlencoded";
    public static final String DEFAULT_URL = "http://dev.stianj.com:1337/message";

    @InjectDependency
    RESTClient mRestClient;

    @Override
    public String describeActuator() {
        return "HTTP Actuator";
    }

    @Override
    public String describeArguments(JSONObject arguments) {
        try {
            return "Posts " + arguments.getString(DATA) + " to " + arguments.getString(URL);
        } catch (JSONException e) {
            e.printStackTrace();
            return "Invalid arguments for actuator";
        }
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    void actuate(final JSONObject arguments) throws JSONException {
        new SimpleAsyncTask<Void>(mContext, null) {
            @Override
            protected Void onExecute() throws Exception {
                mRestClient.post(arguments.getString(URL),
                        ENCODING,
                        arguments.getString(DATA));
                return null;
            }
        }.execute();
    }

    @Override
    public AlertDialog getActuatorDialog(Activity activity, final Action action, final Rule rule, final ActuatorDialogFinishListener actuatorDialogFinishListener) {
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = layoutInflater.inflate(R.layout.dialog_actuator_double_textinput, null);

        final EditText editText1 = (EditText) view.findViewById(R.id.etDialogActuatorEditText1);
        editText1.setHint(URL);
        editText1.setText(DEFAULT_URL);

        final EditText editText2 = (EditText) view.findViewById(R.id.etDialogActuatorEditText2);
        editText2.setHint(DATA);
        editText2.setText("message=");

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setView(view)
                .setTitle(describeActuator())
                .setPositiveButton(activity.getString(R.string.accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String arguments = Action.jsonStringBuilder(
                                URL, editText1.getText().toString(),
                                DATA, editText2.getText().toString());

                        action.setArguments(arguments);
                        rule.addAction(action);
                        actuatorDialogFinishListener.onActuatorDialogFinish(action, rule);
                    }
                })
                .setNegativeButton(activity.getString(R.string.reject), null);
        return builder.create();
    }
}
