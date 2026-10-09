package vn.edu.vhu.ltdd.a5intent;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "A5_241A010541";
    public static final String EXTRA_CONTACT = "extra_contact";
    public static final String EXTRA_NGUOI_GUI = "extra_nguoi_gui";
    public static final String EXTRA_DS_CONTACT = "extra_ds_contact"; // Khóa truyền danh sách cho NC2

    private EditText edtHoTen, edtDienThoai, edtEmail;
    private TextView tvKetQuaTraVe;

    private final ActivityResultLauncher<Intent> chiTietLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Contact daSua = IntentCompat.getParcelableExtra(
                            result.getData(), EXTRA_CONTACT, Contact.class);
                    if (daSua != null) {
                        edtHoTen.setText(daSua.getHoTen());
                        tvKetQuaTraVe.setText(getString(R.string.returned, daSua.getHoTen()));
                        Log.d(TAG, "Nhận kết quả trả về: " + daSua.getHoTen());
                    }
                } else {
                    tvKetQuaTraVe.setText(R.string.returned_cancel);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity - onCreate");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        edtHoTen = findViewById(R.id.edtHoTen);
        edtDienThoai = findViewById(R.id.edtDienThoai);
        edtEmail = findViewById(R.id.edtEmail);
        tvKetQuaTraVe = findViewById(R.id.tvKetQuaTraVe);

        findViewById(R.id.btnChiTiet).setOnClickListener(v -> moManHinhChiTiet());
        findViewById(R.id.btnGoi).setOnClickListener(v -> goiDien());
        findViewById(R.id.btnWeb).setOnClickListener(v -> moTrangWeb());
        findViewById(R.id.btnChiaSe).setOnClickListener(v -> chiaSe());

        // Gọi hàm xử lý nhận văn bản chia sẻ khi chạy app lần đầu
        xuLyVanBanNhanDuoc(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // Gọi hàm xử lý khi app đang chạy mà có dữ liệu chia sẻ mới gửi tới
        xuLyVanBanNhanDuoc(intent);
    }

    // ============ BÀI NÂNG CAO 4 (NC4): Xử lý văn bản chia sẻ từ app khác ============
    private void xuLyVanBanNhanDuoc(Intent intent) {
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())) {
            String vanBanChiaSe = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (vanBanChiaSe != null) {
                edtHoTen.setText(vanBanChiaSe);
                Toast.makeText(this, "Đã nhận văn bản chia sẻ!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Các hàm lifecycle để quan sát Logcat
    @Override protected void onStart() { super.onStart(); Log.d(TAG, "MainActivity - onStart"); }
    @Override protected void onResume() { super.onResume(); Log.d(TAG, "MainActivity - onResume"); }
    @Override protected void onPause() { super.onPause(); Log.d(TAG, "MainActivity - onPause"); }
    @Override protected void onStop() { super.onStop(); Log.d(TAG, "MainActivity - onStop"); }
    @Override protected void onRestart() { super.onRestart(); Log.d(TAG, "MainActivity - onRestart"); }
    @Override protected void onDestroy() { super.onDestroy(); Log.d(TAG, "MainActivity - onDestroy"); }

    private void moManHinhChiTiet() {
        String hoTen = edtHoTen.getText().toString().trim();
        if (hoTen.isEmpty()) {
            edtHoTen.setError(getString(R.string.err_empty));
            return;
        }
        Contact contact = new Contact(hoTen,
                edtDienThoai.getText().toString().trim(),
                edtEmail.getText().toString().trim());

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(EXTRA_CONTACT, contact);
        intent.putExtra(EXTRA_NGUOI_GUI, TAG);

        // ============ BÀI NÂNG CAO 2 (NC2): Đóng gói và truyền ArrayList<Contact> ============
        ArrayList<Contact> dsContact = new ArrayList<>();
        dsContact.add(contact);
        dsContact.add(new Contact("Liên hệ phụ 1", "0909111222", "phu1@gmail.com"));
        dsContact.add(new Contact("Liên hệ phụ 2", "0909333444", "phu2@gmail.com"));
        intent.putParcelableArrayListExtra(EXTRA_DS_CONTACT, dsContact);

        chiTietLauncher.launch(intent);
    }

    private void goiDien() {
        String sdt = edtDienThoai.getText().toString().trim();
        if (sdt.isEmpty()) {
            edtDienThoai.setError(getString(R.string.err_empty));
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + sdt));
        moAnToan(intent);
    }

    private void moTrangWeb() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.school_url)));
        moAnToan(intent);
    }

    private void chiaSe() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text,
                edtHoTen.getText().toString(), edtDienThoai.getText().toString()));
        startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
    }

    private void moAnToan(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.err_no_app, Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Không có ứng dụng nào xử lý: " + intent.getAction(), e);
        }
    }
}