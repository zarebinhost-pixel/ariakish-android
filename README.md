# آریا کیش Android 1.0.1

اپلیکیشن Native Android فروشگاه آریا کیش با **Kotlin + Jetpack Compose**.

## وضعیت پروژه
- اتصال به WooCommerce Store API
- رابط فارسی RTL و ریسپانسیو
- محصولات، دسته‌بندی‌ها، جستجو، صفحه محصول، علاقه‌مندی و سبد خرید
- پشتیبانی از محصولات متغیر از طریق WordPress Bridge
- ورود/ثبت‌نام ایمیلی، پروفایل، آدرس و سفارش‌ها
- آماده‌سازی Google Login
- Checkout واقعی WooCommerce در Chrome Custom Tab
- مدیریت بنرها از وردپرس
- Build خودکار APK با GitHub Actions

## خروجی APK بدون Android Studio
بعد از هر Push به شاخه `main`، Workflow با نام **Build Ariakish Android APK** اجرا می‌شود. بعد از سبز شدن Build، وارد همان Run شوید و از بخش **Artifacts** فایل `ariakish-final-package` را دانلود کنید. داخل ZIP فایل `ariakish-v1.0.1.apk` قرار دارد.

## نصب افزونه وردپرس
سورس افزونه در مسیر `wordpress-plugin/ariakish-app-bridge` قرار دارد. Workflow علاوه بر APK، فایل ZIP قابل نصب افزونه را هم داخل Artifact قرار می‌دهد.

بعد از نصب افزونه این آدرس باید JSON معتبر برگرداند:

`https://ariakish-89.ir/wp-json/ariakish/v1/health`

## تنظیمات API
Store API:

`https://ariakish-89.ir/wp-json/wc/store/v1/`

Bridge API:

`https://ariakish-89.ir/wp-json/ariakish/v1/`

هیچ WooCommerce Consumer Secret داخل APK یا Repository قرار نمی‌گیرد.

## Google Login
برای فعال‌شدن Google Login باید Android OAuth Client برای پکیج `ir.ariakish.app` و یک Web OAuth Client در Google Cloud ساخته شود. Web Client ID از تنظیمات افزونه وردپرس خوانده می‌شود. کلید Release و رمزهای آن نباید در Repository عمومی قرار گیرند.

## انتشار نهایی
APK خروجی GitHub برای تست و نصب مستقیم مناسب است. برای انتشار در مارکت‌ها باید Release/AAB با keystore اختصاصی و Secrets گیت‌هاب ساخته شود.

---
طراحی و توسعه: **فرادید پرهام**  
https://www.faradidparham.com
