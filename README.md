Warning
=======

There is already a nice open-source implementation of this application on android: [SMSSync](https://github.com/ushahidi/SMSSync)



sms-db-slave
============

An Android application that sends and receives SMS and update to sms-db.


Install and configure
=====================

* API URL for `sms_received` - Received SMS will be POST-ed to this API
* API URL for `pending` - A URL to fetch SMS that are waiting to be sent
* API URL for `sent` - When SMS is sent, POSTs the id to this API
* API Key - Every SMS terminal has to have its specific api key. It has to
  be passed whenever an API request is made.
* Crash report URL - An acralyzer application URL to POST submit report
  to.
* Crash report username
* Crash report password


How does it work?
=================

There are 3 entities interacting throughout this application.
* [SMSDB](https://github.com/ariunbayar/sms-db) - Stores SMS that are received and to be sent
* [SQLite](http://developer.android.com/reference/android/database/sqlite/package-summary.html) - Stores SMS that are being sent or received. Whenever
  Mobile network or Internet failure occurs, it will retry the stored SMS.
* Periodic Service:
    - Send - Fetch pending SMS from SMSDB and save to DB
    - Send - Get pending SMS from DB and send. Delivery report will set the SMS
      state to sent in DB. 
    - Send - Get pending SMS which has been successfully delivered from DB
      and post to SMSDB
    - Receive - Triggered by `android.provider.telephony.SMS_RECEIVED`. Saves
      received SMS to DB.
    - Receive - Get received SMS from DB and posts to SMSDB
    - Cleanup - Remove SMS that are marked as synced

Following types of SMS might occur in the SQLite Database:

    +----+------+--------------+---------------------+----------+--------+--------+
    | id | body |    phone     |     created_at      |  status  | sms_id | synced |
    +----+------+--------------+---------------------+----------+--------+--------+
    | 1  | abc  | +97698111111 | 2014-02-06 08:24:00 | to_send  |   71   |   0    |
    | 1  | abc  | +97698111111 | 2014-02-06 08:24:00 | sending  |   71   |   0    |
    | 1  | abc  | +97698111111 | 2014-02-06 08:24:00 |   sent   |   71   |   0    |
    | 1  | abc  | +97698111111 | 2014-02-06 08:24:00 |   sent   |   71   |   1    |
    | 1  | abc  | +97698111111 | 2014-02-06 08:24:00 | send_fail|   71   |   0    |
    | 1  | abc  | +97698111111 | 2014-02-06 08:24:00 | send_fail|   71   |   1    |
    | 2  | foo  | +97688776655 | 2014-02-07 09:00:00 | received |  null  |   0    |
    | 2  | foo  | +97688776655 | 2014-02-07 09:00:00 | received |   78   |   1    |
    +----+------+--------------+---------------------+----------+--------+--------+

