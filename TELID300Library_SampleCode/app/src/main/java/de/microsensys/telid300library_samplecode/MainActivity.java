package de.microsensys.telid300library_samplecode;

import static de.microsensys.telid300interface.helper.Constants.Humidity;
import static de.microsensys.telid300interface.helper.Constants.Light;
import static de.microsensys.telid300interface.helper.Constants.Pressure;
import static de.microsensys.telid300interface.helper.Constants.Shock;
import static de.microsensys.telid300interface.helper.Constants.Temperature;
import static de.microsensys.telid300interface.helper.Constants.UvIndex;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.microsensys.telid300interface.TELIDDataProtocol;
import de.microsensys.telid300interface.TELIDInformation;
import de.microsensys.telid300interface.TELIDLoggerCallback;
import de.microsensys.telid300interface.TELIDLoggerHandler;
import de.microsensys.telid300interface.TELIDProgramParameters;
import de.microsensys.telid300interface.TELIDStateInformation;

public class MainActivity extends AppCompatActivity {

    TextView mTextViewTop;
    Button mButtonReadLog;
    Button mButtonProgram;
    EditText mTextViewResults;
    EditText mTextViewLogMeasurements;

    private NfcAdapter mNfcAdapter = null;
    private TELIDLoggerHandler mCommHandler;

    private TELIDInformation mTelid;
    private TELIDStateInformation mTelidStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //UI variables & implementation
        mTextViewTop = findViewById(R.id.textViewTop);
        mTextViewTop.setText("Created");
        mButtonProgram = findViewById(R.id.button_Program);
        mButtonProgram.setOnClickListener(v -> {
            Calendar startTime = Calendar.getInstance();
            startTime.set(Calendar.SECOND, 0);
            startTime.add(Calendar.MINUTE, 10);
            Calendar stopTime = (Calendar)startTime.clone();
            stopTime.add(Calendar.DAY_OF_MONTH, 1);
            TELIDProgramParameters progParams = new TELIDProgramParameters(
                    startTime,
                    stopTime,
                    60,
                    10,
                    30,
                    0,
                    0xFF,
                    "remarks");
            if (mCommHandler != null){
                if (mCommHandler.startProgram(mTelid, mTelidStatus, progParams)){
                    mButtonProgram.setEnabled(false);
                    mButtonReadLog.setEnabled(false);
                    mTextViewResults.append("Programming started...\n");
                }
            }
        });
        mButtonProgram.setEnabled(false);
        mButtonReadLog = findViewById(R.id.button_ReadLog);
        mButtonReadLog.setOnClickListener(v -> {
            if (mCommHandler != null){
                try {
                    if (mCommHandler.startReadProtocol(mTelid, mTelidStatus)) {
                        mButtonProgram.setEnabled(false);
                        mButtonReadLog.setEnabled(false);
                        mTextViewResults.append("ReadProtocol started...\n");
                    }
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        mButtonReadLog.setEnabled(false);
        mTextViewResults = findViewById(R.id.editText_Results);
        mTextViewLogMeasurements = findViewById(R.id.editText_Measurements);
        mTextViewResults.append("Lib Version: " + de.microsensys.telid300interface.LibraryVersion.getVersionNumber());

        //Get NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null){
            //NFC is not available in this device!! --> Show Toast and close the App
            Toast toast = Toast.makeText(this.getBaseContext(), "NFC is not available", Toast.LENGTH_LONG);
            toast.show();
            finish();
            return;
        }

        //Initiailze TELIDLoggerHandler
        mCommHandler = new TELIDLoggerHandler(mTelidCallback);
        //Get StartIntent in case the App was started by OS because a TELID was found
        Intent startIntent = getIntent();
        if (startIntent != null){
            //Check if the App started by NDEF Found event, and if true, automatically start reading the status
            mCommHandler.checkStartupIntent(startIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Check NFC is enabled
        if (!mNfcAdapter.isEnabled()){
            //NFC is disabled --> Ask for activation.
            //  To enable --> open Settings
            //  If no wish to enable --> finish Activity
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("NFC is disabled")
                    .setMessage("Enable NFC?\n(Selecting NO will close the App)")
                    .setPositiveButton("YES", (dialog, which) -> startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS)))
                    .setNegativeButton("NO", (dialog, which) -> finish());
            builder.create().show();
        }
        else {
            //Enable Search
            if (!mCommHandler.enableTelidSearch(mNfcAdapter, this)) {
                Toast toast = Toast.makeText(this.getBaseContext(), "NFC Search could not be enabled", Toast.LENGTH_LONG);
                toast.show();
            }
            mTextViewTop.setText("TELID Search started...");
        }
    }

    @Override
    protected void onPause() {
        //Disable Search
        mCommHandler.disableTelidSearch(mNfcAdapter, this);
        super.onPause();
    }

    private void setTopTextView(final String _text){
        new Handler(Looper.getMainLooper()).post(() -> mTextViewTop.setText(String.format("%s\n", _text)));
    }
    private void appendResultText(final String _toAppend){
        new Handler(Looper.getMainLooper()).post(() -> mTextViewResults.append(_toAppend + "\n"));
    }
    //
    private void setMeasurementsText(final String _text){
        new Handler(Looper.getMainLooper()).post(() -> mTextViewLogMeasurements.setText(String.format("%s\n", _text)));
    }
    private void setButtonsEnabled(final boolean _enabled){
        new Handler(Looper.getMainLooper()).post(() -> {
            mButtonProgram.setEnabled(_enabled);
            mButtonReadLog.setEnabled(_enabled);
        });
    }

    TELIDLoggerCallback mTelidCallback = new TELIDLoggerCallback() {
        @Override
        public void telidLost() {
            appendResultText("TAG LOST!!");
            setTopTextView("TELID Lost. Searching...");
            setButtonsEnabled(false);
        }

        @Override
        public void telidFound(TELIDInformation telidInformation) {
            StringBuilder toAppend = new StringBuilder("\n\nTELID FOUND\n");
            toAppend.append(" ID= ").append(telidInformation.getTelidSerialNumber()).append("\n");
            toAppend.append(" Type= ").append(telidInformation.getTelidTypeString()).append("\n");
            //toAppend += " Description= " + telidInformation.getTelidDescription() + "\n";
            toAppend.append(" Physical data:\n");
            int[] phData = telidInformation.getTelidPhyisicalData();
            for(int i : phData){
                toAppend.append("   - ");
                switch (i){
                    case Temperature:
                        toAppend.append("Temperature");
                        break;
                    case Shock:
                        toAppend.append("Shock");
                        break;
                    case Humidity:
                        toAppend.append("Humidity");
                        break;
                    case Pressure:
                        toAppend.append("Pressure");
                        break;
                    case UvIndex:
                        toAppend.append("UV Index");
                        break;
                    case Light:
                        toAppend.append("Light");
                        break;
                }
                toAppend.append("\n");
            }
            appendResultText(toAppend.toString());
            setMeasurementsText("");
            setTopTextView("TELID®" + telidInformation.getTelidTypeString() + " present: ID= " + telidInformation.getTelidSerialNumber());
        }

        @Override
        public void telidReadingMeasurements(int _numRead, int _numTotal) {
            int percentage = _numRead * 100 / _numTotal;
            appendResultText("Read: " + percentage + "% (" + _numRead + " / " + _numTotal + ")");
        }

        @Override
        public void telidStatusRead(TELIDInformation telidInformation, TELIDStateInformation telidStateInformation) {
            String toAppend = "TELID STATUS READ\n";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            toAppend += " Logging: " + telidStateInformation.getStatus_isLogging() + "\n";
            toAppend += " MeasLogged: " + telidStateInformation.getNumberLoggedMeasurements() + "\n";
            toAppend += " Start: " + dateFormat.format(telidStateInformation.getProgrammedStartTime().getTime()) + "\n";
            toAppend += " Interval (s): " + telidStateInformation.getIntervalInSeconds() + "\n";
            toAppend += " MaxMeasurements: " + telidStateInformation.getMaxNumberMeasurements() + "\n";
            toAppend += " BatState: " + telidStateInformation.getBatteryState() + "\n";
            toAppend += " Stop: " + dateFormat.format(telidStateInformation.getExpectedStopTime().getTime()) + "\n";
            toAppend += " LimitMin: " + telidStateInformation.getLimitMin() + "\n";
            toAppend += " LimitMax: " + telidStateInformation.getLimitMax() + "\n";
            toAppend += " Remarks: "  + telidStateInformation.getRemarks() + "\n";
            appendResultText(toAppend);

            mTelid = telidInformation;
            mTelidStatus = telidStateInformation;
            setButtonsEnabled(true);
        }

        @Override
        public void telidReadCompleted(TELIDInformation telidInformation, TELIDDataProtocol[] telidDataProtocols) {
            appendResultText("TELID READ COMPLETED\n");
            appendResultText("Meas Read: " + telidDataProtocols.length + "\n");
            StringBuilder toAppend = new StringBuilder();
            for(int i=0; i<telidDataProtocols.length; i++){
                //Get Timestamp
                toAppend.append(" ").append(i).append(" - ").append(telidDataProtocols[i].getTimestampString()).append(": ").append("\n");
                toAppend.append("  ").append("Status: ").append(telidDataProtocols[i].getState()).append("\n");
                toAppend.append("  ").append("BatStatus: ").append(telidDataProtocols[i].getBatState()).append("\n");
                TELIDDataProtocol.SensorValue[] sensorValues = telidDataProtocols[i].getSensorValues();
                for(TELIDDataProtocol.SensorValue sv : sensorValues){
                    toAppend.append("   (").append(sv.getType()).append(") -> ").append(sv.getValue()).append(sv.getUnit()).append("\n");
                }
            }
            setMeasurementsText(toAppend.toString());
            setButtonsEnabled(true);
        }

        @Override
        public void teildProgrammed(TELIDInformation telidInformation) {
            appendResultText("TELID PROGRAMMED!!");
            setButtonsEnabled(true);
            mTelidStatus = null; //Programed --> status probably changed. To read status tap TELID again
        }
    };
}