package com.example.movie;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;

public class ReservationActivity extends AppCompatActivity {

    private static String TAG = "예약";
    private static int SELECT_SEAT = 1;

    /* 파이어베이스 */
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    DatabaseReference ref;
    
    // 영화관 날짜 리스트
    ArrayList<String> dateList;

    // 영화 예약된 좌석 정보
    private ArrayList<Integer> reservedSeat;    // 이미 예약됨
    private ArrayList<Integer> reservingSeat;   // 예약중
    private boolean isSeatLoaded = false;

    // 극장(임시, 강동 메가박스)
    private String theater = "강동 메가박스";     // TODO 중요

    // 파이어베이스의 데이터 중 오늘 날짜의 인덱스
    private int selectedDateIndex = 0;


    /* UI */
    // 날짜선택
    private MaterialDatePicker<Long> datePicker;
    private TextView selectedDateText;


    /* 선택된 정보들 */
    // 날짜
    private int selectedYear;
    private int selectedMonth;
    private int selectedDate;
    private String selectedTime;    // 선택한 예매 날짜

    // 상영 시간
    private ArrayList<String> times = new ArrayList<>();
    private int selectedTimePosition = 0;
    private String selectedTimeTableItem;   // 선택된 상영시간

    // 인원
    private int numOfAdult;
    private int numOfChild;

    // 좌석
    private HashSet<Integer> selectedSeatList;  // 선택한 좌석 리스트
    
    // 가격(영수증)
    private TextView receiptCalculate;
    private TextView receiptResult;
    View border;    // 경계선
    private int paymentAmount = 0;  // 결제액

    // 내 예약 식별번호(id)
    private Integer myReservationNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // 영화 정보
        int id = getIntent().getIntExtra("id", 0);
        String name = getIntent().getStringExtra("name");
        String director = getIntent().getStringExtra("director");
        String content = getIntent().getStringExtra("content");
        String imageName = getIntent().getStringExtra("imageName");

        getSupportActionBar().setTitle(name);

        // 영화 예약 관련 정보 받아오기
        ref = database.child("theater").child("0").child("movie").child(String.valueOf(id));

        // TODO 날짜 정보 받아오기
        dateList = new ArrayList<>();
        ref.child("date").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot child: task.getResult().getChildren())
                    dateList.add(child.getValue(String.class));
                Log.d(TAG, "날짜 정보 다운로드: " +dateList.toString());
                
                // 날짜 초기화
                initDateText();
            }
        });

        // 극장 선택
        TextView theaterText = findViewById(R.id.theaterText);
        theaterText.setText(theater);

        // 인원 선택
        Spinner adultSpinner = findViewById(R.id.adultSpinner);
        initSpinner(adultSpinner, true);
        Spinner childSpinner = findViewById(R.id.childSpinner);
        initSpinner(childSpinner, false);

        // 결제 텍스트
        receiptCalculate = findViewById(R.id.receiptCalculate);
        receiptResult = findViewById(R.id.receiptResult);
        receiptResult.setText("총 " + paymentAmount + "원");

        border = findViewById(R.id.border);
        border.setBackgroundColor(receiptCalculate.getCurrentTextColor());
    }

    /* TODO 최종 확인 버튼 (결제 여기다가 넣으면 됨) */
    public void finalConfirm(View v) {
        if (isAllOk()) {
            updateReservation();
            Toast.makeText(this, "결제 확인버튼 클릭", Toast.LENGTH_SHORT).show();
            // 여기다 넣어주세요





            this.finish();
        }
    }


    private boolean isAllOk() {
        if (selectedTime == null || selectedDateText.getText().toString().equals("-"))
            Toast.makeText(this, "날짜를 선택해주세요", Toast.LENGTH_SHORT).show();
        else if (numOfChild + numOfAdult == 0)
            Toast.makeText(this, "인원을 선택해주세요", Toast.LENGTH_SHORT).show();
        else if (selectedSeatList == null || selectedSeatList.size() == 0)
            Toast.makeText(this, "좌석을 선택해주세요", Toast.LENGTH_SHORT).show();
        else if (selectedTimeTableItem == null)
            Toast.makeText(this, "상영시간을 다시 선택해주세요", Toast.LENGTH_SHORT).show();
        else if (paymentAmount == 0)
            Toast.makeText(this, "결제액이 0원입니다", Toast.LENGTH_SHORT).show();
        else
            return true;

        return false;
    }

    // 예약좌석 데이터베이스에 업데이트
    private void updateReservation() {
        DatabaseReference reservationRef = ref.child("time").child(String.valueOf(selectedDateIndex))
                .child("reservation").child(String.valueOf(selectedTimePosition));

        for (Integer selectedSeatNum : selectedSeatList) {
            reservationRef.child("the number of seats reserved").runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Integer numOfReserved = currentData.getValue(Integer.class);
                    if (numOfReserved == null)
                        currentData.setValue(1);
                    else
                        currentData.setValue(numOfReserved + 1);
                    return Transaction.success(currentData);
                }
                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    myReservationNumber = currentData.getValue(Integer.class);
                    reservationRef.child("reserved").child(String.valueOf(myReservationNumber)).setValue(selectedSeatNum);
                }
            });
        }
    }
    
    // 상영시간표 초기화
    private void initTimeTable() {
        final int[] selectedItem = new int[1];

        ListView timeTable = findViewById(R.id.timeTable);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, times) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = View.inflate(getApplicationContext(), android.R.layout.simple_list_item_1, null);

                if (position == selectedItem[0])
                    view.setBackgroundColor(Color.LTGRAY);

                TextView textView = view.findViewById(android.R.id.text1);
                textView.setText(times.get(position));

                return view;
            }
        };

        timeTable.setAdapter(adapter);
        timeTable.setOnItemClickListener((parent, view, position, id) -> {
            selectedItem[0] = position;
            selectedTimePosition = position;
            adapter.notifyDataSetChanged();
            selectedTimeTableItem = times.get(selectedTimePosition);

            Log.d(TAG, "상영시간 선택: " + times.get(selectedTimePosition));
            initReservedSeat();

            // 좌석이 선택된 후에 인원수를 바꾸면 좌석 초기화
            if (selectedSeatList != null) {
                selectedSeatList = null;
                TextView selectedSeatText = findViewById(R.id.selectedSeatText);
                selectedSeatText.setText("-");
            }
        });
        
        // TODO 타임 테이블 받아오기
        DatabaseReference timeTableRef = ref.child("time").child(String.valueOf(selectedDateIndex)).child("time table");
        timeTableRef.get().addOnCompleteListener(task -> {
            times.clear();
            if (task.isSuccessful()) {
                for (DataSnapshot child: task.getResult().getChildren()) {
                    times.add(child.getValue(String.class));
                }
            }
            Log.d(TAG, "타임 테이블 다운로드: " + times.toString());
            adapter.notifyDataSetChanged();
        });
    }

    //  예약된 좌석 정보 초기화
    private void initReservedSeat() {
        DatabaseReference reservationRef = ref.child("time").child(String.valueOf(selectedDateIndex))
                .child("reservation").child(String.valueOf(selectedTimePosition));

        reservationRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    // 이미 예약된 좌석
                    if (reservedSeat == null)
                        reservedSeat = new ArrayList<>();
                    else
                        reservedSeat.clear();
                    for (DataSnapshot child : task.getResult().child("reserved").getChildren()) {
                        reservedSeat.add(child.getValue(int.class));
                    }

                    // 에약중인 좌석
                    if (reservingSeat == null)
                        reservingSeat = new ArrayList<>();
                    else
                        reservingSeat.clear();
                    for (DataSnapshot child : task.getResult().child("reserving").getChildren()) {
                        reservingSeat.add(child.getValue(int.class));
                    }
                    Log.d(TAG, "에약된 좌석 불러오기: " + reservedSeat + " and 에약중인 좌석 불러오기: " + reservedSeat);
                    isSeatLoaded = true;
                }
            }
        });

    }

    // 스피너 초기화
    private void initSpinner(Spinner spinner, boolean isAdult) {
        Integer[] numOfPeople = { 0, 1, 2, 3, 4 };
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, numOfPeople);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 좌석이 선택된 후에 인원수를 바꾸면 좌석 초기화
                if (selectedSeatList != null) {
                    selectedSeatList = null;
                    TextView selectedSeatText = findViewById(R.id.selectedSeatText);
                    selectedSeatText.setText("-");
                }
                if (isAdult)
                    numOfAdult = numOfPeople[position];
                else
                    numOfChild = numOfPeople[position];

                // 경계선
                if (numOfAdult + numOfChild == 0)
                    border.setVisibility(View.GONE);
                else
                    border.setVisibility(View.VISIBLE);

                // 가격 계산
                StringBuffer buffer = new StringBuffer();
                if (numOfAdult != 0)
                    buffer.append("성인 ").append(numOfAdult).append("명 * 8000원");
                if (numOfChild != 0) {
                    if (numOfAdult != 0)
                        buffer.append("\n청소년 ").append(numOfChild).append("명 * 6000원");
                    else
                        buffer.append("청소년 ").append(numOfChild).append("명 * 6000원");
                }

                receiptCalculate.setText(buffer.toString());

                // 가격 결과
                paymentAmount = numOfAdult * 8000 + numOfChild * 6000;
                receiptResult.setText("총 " + paymentAmount + "원");
                Log.d(TAG, "인원 선택 성인 " + numOfAdult + ", 청소년 " + numOfChild + "명");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    // 날짜 텍스트 뷰 초기화
    private void initDateText() {
        if (selectedDateText == null)
            selectedDateText = findViewById(R.id.selectedDateText);

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH) + 1;
        selectedDate = calendar.get(Calendar.DATE);

        String today = selectedYear + "년 " + selectedMonth + "월 " + selectedDate + "일";

        int firebaseTodayIndex = -1;

        for (int i = 0; i < dateList.size(); i++) {
            if (today.equals(dateList.get(i))) {
                firebaseTodayIndex = i;
                break;
            }
        }

        if (firebaseTodayIndex < 0) {
            Log.d(TAG, "오늘: " + today + "파이어베이스에서 받아온 날짜 정보에 오늘 날짜가 없음. initDateText() 일단 중지함.");
            return;
        }

        for (int i = firebaseTodayIndex - 1; i > 0; i--) {
            dateList.remove(i);
        }

        selectedDateText.setText("-");
    }

    // 좌석 선택 액티비티 실행
    public void selectSeat(View v) {
        if (selectedTime == null || selectedDateText.getText().toString().equals("-")) {
            Toast.makeText(this, "먼저 날짜를 선택해주세요", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "날짜가 미리 선택되있지 않아서 좌석선택 취소");
            return;
        }

        if (numOfAdult + numOfChild == 0) {
            Toast.makeText(this, "좌석에 앉을 인원을 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, SeatActivity.class);
        intent.putExtra("numOfPeople", numOfAdult + numOfChild);

        if (reservedSeat == null || reservingSeat == null) {
            selectedTimePosition = 0;
            selectedTimeTableItem = times.get(selectedTimePosition);
            initReservedSeat();
            Log.d(TAG, "상영시간표가 선택이 안되어서 맨 위에거라 치고 초기화 시키기");
        }

        if (selectedSeatList != null)
            intent.putExtra("selectedSeatList", selectedSeatList);
        else
            intent.putExtra("selectedSeatList", new HashSet<>());

        intent.putExtra("reservedSeat", reservedSeat);
        intent.putExtra("reservingSeat", reservingSeat);

        if (!isSeatLoaded) {
            Toast.makeText(this, "잠시뒤에 다시 시도해주세요", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "좌석 예약 정보가 아직 덜 다운로드됨");
            return;
        }

        startActivityForResult(intent, SELECT_SEAT);
    }

    // 날짜선택 다이얼로그
    public void showDatePicker(View v) {
        if (datePicker == null) {
            datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("날짜선택")
                    .setCalendarConstraints(limitRange().build())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                selectedYear = calendar.get(Calendar.YEAR);
                selectedMonth = calendar.get(Calendar.MONTH) + 1;
                selectedDate = calendar.get(Calendar.DATE);

                selectedTime = selectedYear + "년 " + selectedMonth + "월 " + selectedDate + "일";

                selectedDateText.setText(selectedTime);

                // 선택된 날짜 인덱스 찾기
                for (int i = 0; i < dateList.size(); i++) {
                    if (selectedTime.equals(dateList.get(i))) {
                        selectedDateIndex = i;
                        break;
                    }
                }

                Log.d(TAG, "날짜 선택: " + selectedTime);
                // TODO 상영시간표 초기화
                initTimeTable();
            });
        }
        datePicker.show(getSupportFragmentManager(), TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_SEAT && data != null) {

            selectedSeatList = (HashSet<Integer>) data.getSerializableExtra("selectedSeatList");
            ArrayList<Integer> temp = new ArrayList<>(selectedSeatList);
            Collections.sort(temp);

            StringBuffer buffer = new StringBuffer();
            int count = 0;
            for (int seat : temp) {
                buffer.append(seat);
                buffer.append("번");
                if (count != temp.size() - 1)
                    buffer.append(", ");
                count++;
            }
            String selectedSeatStr = buffer.toString();
            if (selectedSeatStr.equals(""))
                selectedSeatStr = "-";

            TextView selectedSeatText = findViewById(R.id.selectedSeatText);
            selectedSeatText.setText(selectedSeatStr);
        }
    }

    // 뒤로가기 버튼 누르면 취소 취급
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
        alBuilder.setMessage("영화 예매를 취소하시겠습니까?");

        alBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alBuilder.setTitle("예매 취소");
        alBuilder.show();
    }

    private CalendarConstraints.Builder limitRange() {
        CalendarConstraints.Builder builder = new CalendarConstraints.Builder();
        Calendar start = GregorianCalendar.getInstance();
        Calendar end = GregorianCalendar.getInstance();

        int year = start.get(Calendar.YEAR);
        int month = start.get(Calendar.MONTH);
        int date = start.get(Calendar.DATE) - 1;
        start.set(year, month, date);
        end.set(year, month, date);
        end.add(Calendar.DATE, 7);

        long minDate = start.getTimeInMillis();
        long maxDate = end.getTimeInMillis();

        builder.setStart(minDate);
        builder.setEnd(maxDate);
        builder.setValidator(new RangeValidator(minDate, maxDate));

        return builder;
    }


    private class RangeValidator implements CalendarConstraints.DateValidator {

        private long minDate;
        private long maxDate;

        private Parcelable.Creator<RangeValidator> CREATOR = new Parcelable.Creator<RangeValidator>() {
            @Override
            public RangeValidator createFromParcel(Parcel source) {
                return new RangeValidator(source);
            }
            @Override
            public RangeValidator[] newArray(int size) {
                return new RangeValidator[size];
            }
        };

        public RangeValidator(long minDate, long maxDate) {
            this.minDate = minDate;
            this.maxDate = maxDate;
        }

        public RangeValidator(Parcel parcel) {
            this(parcel.readLong(), parcel.readLong());
        }

        @Override
        public boolean isValid(long date) {
            return !(minDate > date || maxDate < date);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }


}