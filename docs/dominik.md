Notatki z części kodu aplikacji - łączenie się bleutooth z urządzeniem


Polecam pobrać sobie Android Studio, a nawet zainstalować sobie maszynę wirtualną. IDE powinno samo wykryć ustawienia i zainstalować sobie potrzebne SKD, ale jak nie to projekt używa:

```json
{
	"GradleVersion": "8.2.1",
	"Android studio": "Iguana",
	"Android SDK": "14 - \"Upside Down Cake\"",
	"ApiLevel": "34, revision 2",
	"Język": "Kotlin",
}
```

Json tylko aby to ładnie wyświetlić w Markdown, nie ma zastosowania w projekcie.

Polecam sobie na bierząco przeglądać te pliki co tutaj wymieniam. Nie będę się rozpisywał o każdym szczególe, bo to nie ma sensu. Wszystko jest opisane w dokumentacji Androida, a ja opiszę co jest ważne i co warto znać. Kod zawiera też komentarze.

#Struktura

Ponizej opiszę strukturę plików i folderów w aplikacji. Taki widok prezentuje się w Android Studio, ale nie fizycznie w ułożeniu folderów. Niemniej jednak


![MarineGEO circle logo](./1.png "Ogół struktury część 1")

### Manifests
`AndroidManifest.xml` - plik do którego odwołuje się system Android. Zawiera informacje o aplikacji, takie jak nazwa, ikona, wersja, uprawnienia, itp. . Najwarzniejsze są chyba uprawnienia. Dodatkowo jakbyśmy chcieli tą apkę dać na Google Store to trzeby się szczegółowo zainteresować tym plikiem.

### Kotlin+Java

Tu znajduje się cały kod aplikacji, nasza używa w 100% kotlina. Opisz szczeółowy będzie w kolejnym rozdziale. Struktura plików:

- `android.iot` - Główny folder aplikacji. Znajdują się tam wszyskie aktywności aplikacji.

- `android.iot.MainActivity.kt` - Główna aktywność włączana przy starcie aplikacji. Zawiera menu główne aplikacji. T.J.

- `android.iot.lists` - Folder z klasami, które są potrzebne, aby dynamicznie tworzyć listy (*RecyclerViewandroidx.recyclerview.widget.RecyclerView*).

- `android.iot.lists.bluetooth` - Klasy potrzebne do zarządzania listą w layoutcie *activity_paired_device_list.xml* . 


Tutaj są jeszcze testy, ale nie będę się nimi zajmował. (androidTest ....) W skrócie nie są zrobione, bo nie były wymagane


![MarineGEO circle logo](./2.png "Ogół struktury część 2")


bbbbbb

![MarineGEO circle logo](./3.png "Ogół struktury część 3")


cccc




## Permisje

Aplikacja wymaga uprawnień do działania. Są one wymagane przez system Android. W pliku `AndroidManifest.xml` znajduje się lista uprawnień, które są wymagane. Są to:

-	`BLUETOOTH` - Uprawnienie do korzystania z Bluetooth
-	`BLUETOOTH_CONNECT` - Uprawnienie do łączenia się z bluetooth
-	`BLUETOOTH_SCAN` - Uprawnienie do skanowania i identyfikacji pobliskich urządzeń. 

Jeżeli będziemy próbować użyć jakiegoś kodu, który wymaga uprawnień to aplikacja wyrzuci Security Exception i się wykrzaczy.

Jak prosimy o permisje? Właśnie tak:

```kotlin
if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 2)
		Toast.makeText(
			this, "Please enable bluetooth connect to add device!", Toast.LENGTH_LONG
		).show()
		finish()
		return
	}
}
```

Breakdown o co biega tutaj:

- Ten if sprawdza, czy permisja jest przyznana poprzez wywoałanie mteody `checkSelfPermission` z parametrem `android.Manifest.permission.BLUETOOTH_CONNECT`. Jeżeli jest przyznana to zwraca `PackageManager.PERMISSION_GRANTED`, jeżeli nie to `PackageManager.PERMISSION_DENIED`.
	```kotlin
	if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
		// Body
	}
	```

- Ten if sprawdza, czy nasze SDK wpiera (chyba) pytanie usera o permisje. To jest podobne jak sprawdzanie na poziomie #define w CPP, statycznie podanych spraw w kompilacji. Sprawdzamy, czy nasze SKD nie ma w nazwie 29 (nasze zawsze ma 34), ale kod się nie skompiluje bez tego XDD
	```kotlin
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
		// Body
	}
	```

- Tutaj metodą `requestPermissions` wyświetlamy prompta urzytkownikowi o daną permisję i nieważne co odpowie wychodzimy. Dlaczego nie idziemy dalej jak przyzna nam permisję? Bo tam trzeba by zutylizować to 2 na końcu wywołania meody `requestPermissions`. Nie wiem jak to działa. Ludzie też tak mają na stack overflow.
	```kotlin
	ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 2)
	Toast.makeText(
		this, "Please enable bluetooth connect to add device!", Toast.LENGTH_LONG
	).show()
	finish()		//	Finish zamyka aplikację
	return
	```

- Jak chcemy sobie poprosić o inne permisje to wysatrcy podmienić `BLUETOOTH_CONNECT` na coś innego np. `Bluetooth` lub `BLUETOOTH_SCAN`.

- Tam są jeszcze inne ale to musi Michał opisać, bo nie wiem jak działają permisje do Internetu ...

## Bluetooth

### Bluetooth Scan

### Bluetooth Connect