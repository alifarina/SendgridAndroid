# SendgridAndroid
This is a sample for sending email via sendGrid , it resolves the conflict of httpClient used by send-grid java library and can be easily integrated in your project

The original repository has been taken from
https://github.com/danysantiago/sendgrid-android

but the code runs on lower api version gives exception of using legacy HttpClient which android no longer support. So here is some LOC changes to make it work.

Add these lines of code in your build.gradle of your project

dependencies {
.....
    compile 'com.github.danysantiago:sendgrid-android:1',{
        exclude group: 'org.apache.httpcomponents', module: 'httpclient'
    }
}

Voila!!
