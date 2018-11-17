package com.seta.tollroaddroid.app.json;

import android.support.annotation.NonNull;

/**
 * Created by thomashuang on 16-03-04.
 * "datetime": "02/11/2016 17:32",
 "description": "SR261 Irvine Ranch S Lane 1",
 "amount": -1.46
 */
public class RecentToll implements Comparable{
    private String datetime;
    private String description;
    private String amount;
    private String license_plate;
    private String transponder_code;

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getLicense_plate() {
        return license_plate;
    }

    public void setLicense_plate(String license_plate) {
        this.license_plate = license_plate;
    }

    public String getTransponder_code() {
        return transponder_code;
    }

    public void setTransponder_code(String transponder_code) {
        this.transponder_code = transponder_code;
    }
    private int parsingTimeToMins(String time)
    {
        int ret = 0;
        if(time != null)
        {
            int extraHour;
            String newTime;

            if(time.toUpperCase().contains("PM"))
            {
                extraHour = 12;
                newTime= time.replace("PM","");

            }
            else
            {
                extraHour = 0;
                newTime = time.replace("AM","");
            }

            String[] newTimeArray = newTime.split(":");

            if(newTimeArray.length == 2)
            {
                int hour = Integer.valueOf(newTimeArray[0]);
                if(hour == 12)
                {
                    hour = hour - 12;
                }
                ret = (hour+extraHour)*60 + Integer.valueOf(newTimeArray[1]);
            }
        }
        return ret;
    }

//02\/20\/2018 9:56PM
    @Override
    public int compareTo(@NonNull Object another) {
        RecentToll newRecentToll = (RecentToll)another;
        if(this.getDatetime() != null && newRecentToll.getDatetime() != null)
        {
            String[] dateTimeArray1 = this.getDatetime().split(" ");
            String[] dateTimeArray2 = newRecentToll.getDatetime().split(" ");
            if(dateTimeArray1.length == 2 && dateTimeArray2.length == 2)
            {
                String date1 = dateTimeArray1[0];
                String date2 = dateTimeArray2[0];
                String time1 = dateTimeArray1[1];
                String time2 = dateTimeArray2[1];

                if(date1 != null && date2 != null)
                {
                    String[] dateArray1 = date1.split("/");
                    String[] dateArray2 = date2.split("/");

                    if(dateArray1.length == 3 && dateArray2.length == 3)
                    {
                        if(dateArray1[2].equals(dateArray2[2]))
                        {
                            if(dateArray1[0].equals(dateArray2[0]))
                            {
                                if(dateArray1[1].equals(dateArray2[1]))
                                {
                                    int mins1 = parsingTimeToMins(time1);
                                    int mins2 = parsingTimeToMins(time2);

                                    return mins1 - mins2;
                                }
                                else
                                {
                                    return dateArray1[1].compareTo(dateArray2[1]);
                                }
                            }
                            else
                            {
                                return dateArray1[0].compareTo(dateArray2[0]);
                            }
                        }
                        else
                        {
                            return dateArray1[2].compareTo(dateArray2[2]);
                        }
                    }
                }
            }
        }
        return 0;
    }
}
