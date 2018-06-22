package android.sendgrid.sample.com.sendgridandroidsample;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String SENDGRID_USERNAME = "";
    private static final String SENDGRID_PASSWORD = "";
    private static final String SENDGRID_API_KEY = "";
    private static final int ADD_ATTACHMENT = 1;

    // Views
    private EditText toEditText;
    private EditText fromEditText;
    private EditText subjectEditText;
    private EditText msgEditText;
    private Button attachmentButton;
    private TextView attachmentText;
    private Button sendButton;

    // Attachment fields
    private Uri selectedFileUri;
    private String attachmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        toEditText = (EditText) findViewById(R.id.to_editText);
        fromEditText = (EditText) findViewById(R.id.from_editText);
        subjectEditText = (EditText) findViewById(R.id.subject_editText);
        msgEditText = (EditText) findViewById(R.id.message_editText);
        attachmentButton = (Button) findViewById(R.id.attachment_button);
        attachmentText = (TextView) findViewById(R.id.attachment_textView);
        sendButton = (Button) findViewById(R.id.send_button);

        attachmentButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (selectedFileUri == null) {
                    // Start get image intent if no image to attach to email
//          Intent intent = new Intent();
//          intent.setType("image/*");
//          intent.setAction(Intent.ACTION_GET_CONTENT);
//          startActivityForResult(intent, ADD_ATTACHMENT);

                    // way to open file manager and select a file
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");
                    startActivityForResult(intent, ADD_ATTACHMENT);

                } else {
                    // Remove attachment
                    attachmentButton.setText("Add Attachment");
                    attachmentText.setVisibility(View.GONE);
                    selectedFileUri = null;
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start send email ASyncTask
                SendEmailASyncTask task = new SendEmailASyncTask(MainActivity.this,
                        toEditText.getText().toString(),
                        fromEditText.getText().toString(),
                        subjectEditText.getText().toString(),
                        msgEditText.getText().toString(),
                        selectedFileUri,
                        attachmentName);
                task.execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_ATTACHMENT) {
            if (resultCode == RESULT_OK) {
                selectedFileUri = data.getData();
                ContentResolver contentResolver = getContentResolver();
                Log.d("SendAppExample", "File Uri: " + selectedFileUri);

                // Get image attachment filename
                attachmentName = "";
                Cursor c = contentResolver.query(selectedFileUri, null, null, null, null);
                if (c != null && c.moveToFirst()) {
                    attachmentName = c.getString(c.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                }

                // Update views to show attachment
                attachmentText.setVisibility(View.VISIBLE);
                attachmentText.setText(attachmentName);
                attachmentButton.setText("Remove Attachment");
            }
        }
    }

    /**
     * ASyncTask that composes and sends email
     */
    private class SendEmailASyncTask extends AsyncTask<Void, Void, Void> {

        private Context mAppContext;
        private String mMsgResponse;

        private String mTo;
        private String mFrom;
        private String mSubject;
        private String mText;
        private Uri mUri;
        private String mAttachmentName;
        private ProgressDialog mProgressDialog;

        public SendEmailASyncTask(Context context, String mTo, String mFrom, String mSubject,
                                  String mText, Uri mUri, String mAttachmentName) {
            this.mAppContext = context.getApplicationContext();
            this.mTo = mTo;
            this.mFrom = mFrom;
            this.mSubject = mSubject;
            this.mText = mText;
            this.mUri = mUri;
            this.mAttachmentName = mAttachmentName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this, R.style.MaterialTheme);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                SendGrid sendGrid = new SendGrid(SENDGRID_USERNAME, SENDGRID_PASSWORD);

                //Or you can use the send grid key
//                SendGrid sendGrid = new SendGrid(SENDGRID_API_KEY);

                SendGrid.Email email = new SendGrid.Email();

                // Get values from edit text to compose email
                // TODO: Validate edit texts
                email.addTo(mTo);
                email.setFrom(mFrom);
                email.setSubject(mSubject);
                email.setText(mText);

                // Attach image
                if (mUri != null) {
                    email.addAttachment(mAttachmentName, mAppContext.getContentResolver().openInputStream(mUri));
                }

                // Send email, execute http request
                SendGrid.Response response = sendGrid.send(email);
                mMsgResponse = response.getMessage();

                Log.d("SendAppExample", mMsgResponse);

            } catch (SendGridException | IOException e) {
                Log.e("SendAppExample", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mProgressDialog.isShowing()) mProgressDialog.dismiss();

            Toast.makeText(mAppContext, mMsgResponse, Toast.LENGTH_SHORT).show();
        }
    }
}
