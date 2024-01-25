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

- `android.iot.MainActivity.kt` - Główna aktywność włączana przy starcie aplikacji. Zawiera menu główne aplikacji. T.J. 2 przyciski do zmiany tego widoku.

- `android.iot.UserDevices.kt` - Aktywność do zarządzania listą sparowanych urządzeń danego urzytkownika. Wyświtla ją poprzez call do API.

- `android.iot.MainActivity.kt` - Główna aktywność włączana przy starcie aplikacji. Zawiera menu główne aplikacji. T.J.

- `android.iot.MainActivity.kt` - Główna aktywność włączana przy starcie aplikacji. Zawiera menu główne aplikacji. T.J.

- `android.iot.MainActivity.kt` - Główna aktywność włączana przy starcie aplikacji. Zawiera menu główne aplikacji. T.J.

- `android.iot.lists` - Folder z klasami, które są potrzebne, aby dynamicznie tworzyć listy (*RecyclerViewandroidx.recyclerview.widget.RecyclerView*).

- `android.iot.lists.bluetooth` - Klasy potrzebne do zarządzania listą w layoutcie *activity_paired_device_list.xml* . 

- `android.iot.secret` - Folder z klasami, do enkrypcji - zawirają proste funkcje (metody / zmienne statyczne - companion object) do pracy z szyfrowaniem i deszyfrowaniem.

- `android.iot.secret.Encryption.kt` - Plik z ....

- `android.iot.secret.SHA256.kt` - Plik z ....


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

- Tutaj metodą `requestPermissions` wyświetlamy prompta urzytkownikowi o daną permisję i nieważne co odpowie wychodzimy. Dlaczego nie idziemy dalej jak przyzna nam permisję? Bo tam trzeba by zutylizować to 2 na końcu wywołania meody `requestPermissions`. Nie wiem jak to działa. Ludzie też tak mają na stack overflow. Oraz ponieważ nie ma żadnej straty z tego podejścia, ponieważ urzytkownik sobie włączy ponownie jak będzie chciał i jeżeli dalej nie przyzna permisji to wracamy do punktu wyjścia.
	```kotlin
	ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 2)
	Toast.makeText(
		this, "Please enable bluetooth connect to add device!", Toast.LENGTH_LONG
	).show()
	finish()		//	Finish zamyka aplikację
	return
	```

- Jak chcemy sobie poprosić o inne permisje to wysatrcy podmienić `BLUETOOTH_CONNECT` na coś innego np. `BLUETOOTH` lub `BLUETOOTH_SCAN`.

- Tam są jeszcze inne ale to musi Michał opisać, bo nie wiem jak działają permisje do Internetu ...

## Bluetooth

### Bluetooth Scan

!!! Kod do discovery jest tylko w `PairedDeviceListActivity.kt` !!!

Aby móc w aktywności `PairedDeviceListActivity.kt` wyświetlić listę urządeń w pobliżu musimy zacząć proces discovery na naszym adapterze bluetooh. Proces discovery to proces skanowania urządzeń w pobliżu (Wymaga permisji `BLUETOOTH_SCAN`).

Kluczowe informacje o discovery:
-	Rozpoczęcie jest asynchroniczne, tj. `adapter.startDisovery()` zwróci nam odrazu i nie zablokuje głównego threda.
-	Discovery trwa `12 sekund`, po tym czasie jest automatycznie wyłączany.
-	Discovery można wyłączyć wcześniej poprzez wywołanie `adapter.cancelDiscovery()`.
-	Discovery można wyłączyć poprzez wyłączenie bluetooth na urządzeniu.
-	Discovery jest bardzo zasobożerny dla naszego nadajnika bluetooth, więc nie powinno się go używać zbyt często. Ale też nie powinno się go używać podczas próby łączenia z innymi urządzeniami, ponieważ mogą pakiety nie dojść lub połączenie może działać wolno.
-	Discovery nie jest wymagany do połączenia z innym urządzeniem, ale jest wymagany do identyfikacji urządzenia, które chcemy sparować.
- 	Całość działa bardzo podobnie jak zapytanie ARP w sieciach t.j. nasz nadajnik wysyła zapytanie do wszystkich urządzeń w pobliżu, a one odpowiadają. W odpowiedzi dostajemy adresy MAC urządzeń, które są w pobliżu. Wtedy możemy zidentyfikować urządzenie, które chcemy sparować. Dlatego nie ma tej nazwy urządzenia, tylko jego adres MAC (Narzekałem na to kiedyśtam).
-	Więcej info: `https://developer.android.com/develop/connectivity/bluetooth/find-bluetooth-devices#discover-devices`

Zabezpieczenia dla discovery:

-	Za każdym razem kiedy wywołuje `socket.connect()` dla socketu bluetooth, przed tą akcją robie `bluetoothAdapter.cancelDiscovery()`, aby pakiety doszły i nie było problemów z połączeniem.
-	Zawsze instrukcja przed `bluetoothAdapter.startDiscovery()` wywołuje kod poniżej, aby 2 razy tego nie wywołać w głupi sposób:
	```kotlin
	if (bluetoothAdapter.isDiscovering()) {
		bluetoothAdapter.cancelDiscovery()
	}
	```

Ok. Mamy discovery, ale jak uzyskujemy informacje o znalezionych urządzeniach????

W tym celu tworzymy objekt abstrakyjnej klasy `BroadcastReceiver` i overridujemy metodę `onReceive(context: Context, intent: Intent)`. Patrz linia 106 - `PairedDeviceListActivity.kt`.

Musimy jeszcze powiedzieć systemowi, na jakie akcje w komunikacji nasz reciver ma reagować. Robimy to poprzez wywołanie metody `registerReceiver(receiver, filter)`. Patrz linia 150 - `PairedDeviceListActivity.kt`. Filter pozwala na akcje:

Akcje:
-	`BluetoothDevice.ACTION_FOUND` - Urządzenie zostało znalezione w wyniku discovery.
-	`BluetoothAdapter.ACTION_DISCOVERY_STARTED` - Discovery zostało rozpoczęte. Używane do przekazania informacji dla end-usera.
-	`BluetoothAdapter.ACTION_DISCOVERY_FINISHED` - Discovery zostało zakończone. Używane do przekazania informacji dla end-usera.

Na koniec dla bezpieczeństwa wywołujemy `unregisterReceiver(receiver)` w metodzie `onDestroy()`.

### Bluetooth Connect



## Enkrypcja


## Shared preferneces


