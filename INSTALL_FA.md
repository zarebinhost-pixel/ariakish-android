# راهنمای نصب و تنظیم اپلیکیشن آریا کیش – v1.0.1

توسعه: فرادید پرهام – https://www.faradidparham.com

## نصب مستقیم APK روی گوشی
1. فایل `ariakish-v1.0.1.apk` را دانلود کنید.
2. در اندروید، در صورت نمایش هشدار، اجازه «Install unknown apps / نصب برنامه‌های ناشناس» را فقط برای مرورگر یا File Manager مورد استفاده فعال کنید.
3. APK را اجرا و Install را بزنید.
4. نام پکیج برنامه: `ir.ariakish.app`.

## Build در GitHub
Workflow: `Build Ariakish Android APK`

فایل Workflow: `.github/workflows/build-apk.yml`

مشکل قبلی SDK برطرف شده است. Workflow اکنون Android SDK 35 و Build Tools 35 را نصب می‌کند و APK را با Gradle 8.9 / JDK 17 می‌سازد.

برای Build جدید: Repository > Actions > Build Ariakish Android APK > Run workflow. پس از سبز شدن Build، APK از Artifacts و Release قابل دریافت است.

## اتصال فروشگاه
Store API: `https://ariakish-89.ir/wp-json/wc/store/v1/`

Bridge API: `https://ariakish-89.ir/wp-json/ariakish/v1/`

هیچ Consumer Key یا Consumer Secret ووکامرس داخل APK قرار نگرفته است.

## نصب افزونه اتصال وردپرس
فایل `ariakish-app-bridge-v1.0.1.zip` را از وردپرس > افزونه‌ها > افزودن افزونه > بارگذاری افزونه نصب و فعال کنید.

بعد از فعال‌سازی این آدرس باید JSON معتبر و `ok:true` برگرداند:
`https://ariakish-89.ir/wp-json/ariakish/v1/health`

Bridge برای ورود/ثبت‌نام، پروفایل، سفارش‌ها، محصولات متغیر، بنرهای اپ و انتقال امن Checkout استفاده می‌شود.

## Google Login
برای فعال کردن ورود Google، در Google Cloud یک Android OAuth Client برای Package `ir.ariakish.app` و یک Web OAuth Client بسازید. Web Client ID را در تنظیمات افزونه داخل وردپرس وارد کنید. برای انتشار رسمی، SHA-1 کلید امضای Release باید در Google Cloud ثبت شود.

## Checkout و پرداخت
سبد خرید اپ از طریق Bridge به Checkout اصلی WooCommerce منتقل می‌شود؛ روش ارسال، کوپن و درگاه‌های نصب‌شده سایت از همان WooCommerce استفاده می‌شوند.

## انتشار رسمی
APK فعلی برای نصب مستقیم و تست واقعی ساخته شده است. برای انتشار در Google Play یا مارکت‌ها باید Release Keystore اختصاصی داشته باشید و اطلاعات آن در GitHub Secrets نگهداری شود. کلید خصوصی را داخل Repository عمومی قرار ندهید.

---
طراحی و توسعه: فرادید پرهام  
https://www.faradidparham.com
