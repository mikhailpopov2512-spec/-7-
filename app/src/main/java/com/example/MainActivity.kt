package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        DiaryMainContainer()
      }
    }
  }
}

// Pastel & Academic Palette with Glassmorphism Contrast Accent
object DiaryColors {
  val Background = Color(0xFF0F172A) // Deep elegant midnight slate
  val CardBg = Color(0x991E293B)     // Translucent dark slate for glassmorphism
  val LightGlassBg = Color(0xC0FFFFFF) // Light mode glassy card overlay
  val PrimaryText = Color(0xFFF8FAFC)
  val SecondaryText = Color(0xFF94A3B8)

  val GlassBorder = Color(0x66FFFFFF) // Liquid Glass shine border
  val AccentBlue = Color(0xFF3B82F6)
  val AccentPurple = Color(0xFF8B5CF6)
  val AccentGreen = Color(0xFF10B981)
  val AccentRed = Color(0xFFEF4444)
  val AccentOrange = Color(0xFFF59E0B)

  // Mapping color statuses of grades
  val GreenPrimary = Color(0xFF10B981)
  val GreenAccentLight = Color(0x3310B981)

  val OrangePrimary = Color(0xFFF59E0B)
  val OrangeAccentLight = Color(0x33F59E0B)

  val RedPrimary = Color(0xFFEF4444)
  val RedAccentLight = Color(0x33EF4444)

  val BluePrimary = Color(0xFF3B82F6)
  val BlueAccentLight = Color(0x333B82F6)

  val PurplePrimary = Color(0xFF8B5CF6)
  val PurpleAccentLight = Color(0x338B5CF6)

  val WarningPrimary = Color(0xFFF59E0B)
  val WarningAccentLight = Color(0x22F59E0B)

  val BorderLight = Color(0x22FFFFFF)
}

// User role definition
enum class UserRole(val displayName: String, val titleColor: Color, val badgeColor: Color) {
  SENIOR_ADMIN("Старший Администратор", Color(0xFFEF4444), Color(0x55EF4444)),
  JUNIOR_ADMIN("Младший Администратор", Color(0xFF8B5CF6), Color(0x558B5CF6)),
  TEACHER("Учитель Предмета", Color(0xFF10B981), Color(0x5510B981)),
  PARENT("Родитель ученика", Color(0xFFEC4899), Color(0x55EC4899)),
  STUDENT("Ученик (Имя S.)", Color(0xFF3B82F6), Color(0x553B82F6))
}

// Stable models for Compose state management
data class InteractiveStudent(
  val id: String,
  val name: String,
  val gradeClass: String,
  val grades: Map<String, List<String>> = emptyMap() // Subject -> list of grades
)

data class ScheduleLesson(
  val lessonNumber: String,
  val time: String,
  val subject: String,
  val homeworkPlaceholder: String
)

data class SchoolClassStats(
  val className: String,
  val size: Int,
  val averageScore: String,
  val ratingProgress: Float
)

// List of Russian months for our Calendar Picker
val RussianMonths = listOf(
  "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
  "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
)

// Helper core calendar engine (Zeller's congruence for day of the week calculation)
fun getDayOfWeek(day: Int, month: Int, year: Int): Int {
  var q = day
  var m = month
  var y = year
  if (m < 3) {
    m += 12
    y -= 1
  }
  val k = y % 100
  val j = y / 100
  val h = (q + 13 * (m + 1) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
  // Correctly mapping to standard Kotlin Days (1 = Monday, 2 = Tuesday..., 6 = Saturday, 7 = Sunday)
  return when (h) {
    0 -> 6  // Saturday
    1 -> 7  // Sunday
    2 -> 1  // Monday
    3 -> 2  // Tuesday
    4 -> 3  // Wednesday
    5 -> 4  // Thursday
    6 -> 5  // Friday
    else -> 1
  }
}

fun getRussianDayOfWeekName(dayOfWeek: Int): String {
  return when (dayOfWeek) {
    1 -> "Понедельник"
    2 -> "Вторник"
    3 -> "Среда"
    4 -> "Четверг"
    5 -> "Пятница"
    6 -> "Суббота"
    7 -> "Воскресенье"
    else -> "Будни"
  }
}

fun getDaysInMonth(month: Int, year: Int): Int {
  return when (month) {
    1 -> 31
    2 -> if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 29 else 28
    3 -> 31
    4 -> 30
    5 -> 31
    6 -> 30
    7 -> 31
    8 -> 31
    9 -> 30
    10 -> 31
    11 -> 30
    12 -> 31
    else -> 31
  }
}

// Russian School vacation detection engine up to year 2030 included
data class DayStatus(
  val isVacation: Boolean,
  val isHoliday: Boolean,
  val statusName: String
)

fun detectDayStatus(day: Int, month: Int, year: Int): DayStatus {
  // 1. Check fixed major non-working public holidays in Russia
  if (month == 1 && day in 1..8) return DayStatus(true, true, "Новогодние каникулы 🎄")
  if (month == 2 && day == 23) return DayStatus(true, true, "День защитника Отечества 🇷🇺")
  if (month == 3 && day == 8) return DayStatus(true, true, "Международный женский день 🌸")
  if (month == 5 && day == 1) return DayStatus(true, true, "Праздник Весны и Труда 🌱")
  if (month == 5 && day == 9) return DayStatus(true, true, "День Победы 🎖️")
  if (month == 6 && day == 12) return DayStatus(true, true, "День России 🇷🇺")
  if (month == 11 && day == 4) return DayStatus(true, true, "День народного единства 🇷🇺")

  // 2. Check standard Russian academic vacation dates
  // Summer vacation: June 1 to August 31
  if (month in 6..8) return DayStatus(true, false, "Летние каникулы ⛱️")
  
  // Autumn school vacation: late October / early November
  if (month == 10 && day >= 26) return DayStatus(true, false, "Осенние каникулы 🍂")
  if (month == 11 && day <= 3) return DayStatus(true, false, "Осенние каникулы 🍂")

  // Winter school vacation: late December (already partly handled by NY holidays, but custom school cover)
  if (month == 12 && day >= 29) return DayStatus(true, false, "Зимние каникулы ❄️")

  // Spring school vacation: late March
  if (month == 3 && day in 21..30) return DayStatus(true, false, "Весенние каникулы 🌱")

  return DayStatus(false, false, "Рабочий день 📝")
}

// Seeder for unique homework contents so that the calendar up to 2030 displays meaningful tasks for weekdays
fun generateDefaultWeekdayLessons(dayOfWeek: Int, dateKey: String): List<ScheduleLesson> {
  // Return exactly 7 lessons per day as requested
  val seed = dateKey.hashCode()
  val taskSuffix = " (серия #${(seed % 99).coerceAtLeast(1)})"

  return when (dayOfWeek) {
    1 -> listOf(
      ScheduleLesson("Урок 1", "08:30 - 09:15", "Русский язык", "Параграф 14, упр. 152$taskSuffix"),
      ScheduleLesson("Урок 2", "09:25 - 10:10", "Литература", "Прочитать главы 1-3 романа$taskSuffix"),
      ScheduleLesson("Урок 3", "10:25 - 11:10", "Алгебра", "Решить уравнения №234 и №236$taskSuffix"),
      ScheduleLesson("Урок 4", "11:25 - 12:10", "Физика", "Повторить законы Ома в цепи$taskSuffix"),
      ScheduleLesson("Урок 5", "12:20 - 13:05", "История", "Подготовиться к тесту по веку$taskSuffix"),
      ScheduleLesson("Урок 6", "13:15 - 14:00", "Английский яз.", "Translate text on page 78$taskSuffix"),
      ScheduleLesson("Урок 7", "14:10 - 14:55", "Физкультура", "Принести спортивную форму")
    )
    2 -> listOf(
      ScheduleLesson("Урок 1", "08:30 - 09:15", "Геометрия", "Доказать теорему синусов$taskSuffix"),
      ScheduleLesson("Урок 2", "09:25 - 10:10", "Физика", "Лабораторная работа №3$taskSuffix"),
      ScheduleLesson("Урок 3", "10:25 - 11:10", "Английский яз.", "Learn unit 5 vocabulary words$taskSuffix"),
      ScheduleLesson("Урок 4", "11:25 - 12:10", "Химия", "Составить окислительные реакции$taskSuffix"),
      ScheduleLesson("Урок 5", "12:20 - 13:05", "Русский язык", "Подготовить устный доклад$taskSuffix"),
      ScheduleLesson("Урок 6", "13:15 - 14:00", "Биология", "Конспект о строении клетки$taskSuffix"),
      ScheduleLesson("Урок 7", "14:10 - 14:55", "ИЗО", "Принести краски и альбом")
    )
    3 -> listOf(
      ScheduleLesson("Урок 1", "08:30 - 09:15", "Информатика", "Написать цикл на Python$taskSuffix"),
      ScheduleLesson("Урок 2", "09:25 - 10:10", "Алгебра", "Выучить свойства логарифмов$taskSuffix"),
      ScheduleLesson("Урок 3", "10:25 - 11:10", "Обществознание", "Прочитать параграф 7 о праве$taskSuffix"),
      ScheduleLesson("Урок 4", "11:25 - 12:10", "Литература", "Выучить стихотворение поэта$taskSuffix"),
      ScheduleLesson("Урок 5", "12:20 - 13:05", "География", "Нанести на карту столицы Азии$taskSuffix"),
      ScheduleLesson("Урок 6", "13:15 - 14:00", "ОБЖ", "Инструктаж по безопасности$taskSuffix"),
      ScheduleLesson("Урок 7", "14:10 - 14:55", "Физика", "Решить задачи №45-48$taskSuffix")
    )
    4 -> listOf(
      ScheduleLesson("Урок 1", "08:30 - 09:15", "Русский язык", "Повторить суффиксы причастий$taskSuffix"),
      ScheduleLesson("Урок 2", "09:25 - 10:10", "Алгебра", "Контрольная работа №2$taskSuffix"),
      ScheduleLesson("Урок 3", "10:25 - 11:10", "Физика", "Параграф 25, вопросы в конце$taskSuffix"),
      ScheduleLesson("Урок 4", "11:25 - 12:10", "Литература", "Анализ характера Базарова$taskSuffix"),
      ScheduleLesson("Урок 5", "12:20 - 13:05", "Английский яз.", "Past Simple vs Present Perfect$taskSuffix"),
      ScheduleLesson("Урок 6", "13:15 - 14:00", "Химия", "Определить степень окисления$taskSuffix"),
      ScheduleLesson("Урок 7", "14:10 - 14:55", "Физкультура", "Разминка на стадионе")
    )
    5 -> listOf(
      ScheduleLesson("Урок 1", "08:30 - 09:15", "Геометрия", "Объем прямоугольного тела$taskSuffix"),
      ScheduleLesson("Урок 2", "09:25 - 10:10", "Русский язык", "Синтаксический разбор$taskSuffix"),
      ScheduleLesson("Урок 3", "10:25 - 11:10", "Физика", "Подготовка к зачету$taskSuffix"),
      ScheduleLesson("Урок 4", "11:25 - 12:10", "Литература", "Написать творческий отзыв$taskSuffix"),
      ScheduleLesson("Урок 5", "12:20 - 13:05", "История", "Составить хронологию событий$taskSuffix"),
      ScheduleLesson("Урок 6", "13:15 - 14:00", "Английский яз.", "Read article about science$taskSuffix"),
      ScheduleLesson("Урок 7", "14:10 - 14:55", "Биология", "Доклад по генетике человека")
    )
    else -> listOf(
      ScheduleLesson("Урок 1", "08:30 - 09:15", "Профориентация", "Методические консультации$taskSuffix"),
      ScheduleLesson("Урок 2", "09:25 - 10:10", "Право", "Повторение конституции РФ$taskSuffix"),
      ScheduleLesson("Урок 3", "10:25 - 11:10", "Электив Математика", "Сложные уравнения ЕГЭ$taskSuffix"),
      ScheduleLesson("Урок 4", "11:25 - 12:10", "Электив Физика", "Задачи повышенной сложности$taskSuffix"),
      ScheduleLesson("Урок 5", "12:20 - 13:05", "Культура России", "Доклады по искусству$taskSuffix"),
      ScheduleLesson("Урок 6", "13:15 - 14:00", "Астрономия", "Масштабы солнечной системы$taskSuffix"),
      ScheduleLesson("Урок 7", "14:10 - 14:55", "Клуб общения", "Дебаты и круглый стол")
    )
  }
}

// -------------------------------------------------------------
// MAIN CONTAINER COMPOSE
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryMainContainer() {
  // Calendar States with maximum range limit 2026 - 2030
  var selectedYear by remember { mutableIntStateOf(2026) }
  var selectedMonth by remember { mutableIntStateOf(5) } // Default May (Academic month)
  var selectedDay by remember { mutableIntStateOf(26) }   // Default May 26

  val dateKey by remember {
    derivedStateOf { "%04d-%02d-%02d".format(selectedYear, selectedMonth, selectedDay) }
  }

  val activeDayOfWeek by remember {
    derivedStateOf { getDayOfWeek(selectedDay, selectedMonth, selectedYear) }
  }

  val activeDayStatus by remember {
    derivedStateOf { detectDayStatus(selectedDay, selectedMonth, selectedYear) }
  }

  // Active User Roles (Student by default)
  var currentRole by remember { mutableStateOf(UserRole.STUDENT) }
  var currentUserLogin by remember { mutableStateOf("student_user") }
  var isUserLoggedIn by remember { mutableStateOf(false) }

  // Tabs
  var selectedTab by remember { mutableIntStateOf(0) }

  // Announcements state
  var importantTitle by remember { mutableStateOf("Важная новость: Итоговое сочинение") }
  var importantContent by remember { mutableStateOf("Все обучающиеся 11 класса должны явиться в аудиторию 403 к 08:30 в пятницу для финального инструктажа.") }

  // Student list managed in mutableStateListOf for direct database state updates (work offline-first)
  val studentDatabase = remember {
    mutableStateListOf(
      InteractiveStudent("s1", "Алексеев М.", "11-А", mapOf("Геометрия" to listOf("5", "4", "5"), "Физика" to listOf("4", "5"), "Литература" to listOf("5", "3"))),
      InteractiveStudent("s2", "Васильев К.", "11-А", mapOf("Геометрия" to listOf("3", "4", "4"), "Физика" to listOf("3", "2", "3"), "Литература" to listOf("4"))),
      InteractiveStudent("s3", "Иванова О.", "10-Б", mapOf("Геометрия" to listOf("5", "5", "5"), "Физика" to listOf("5", "4"), "Литература" to listOf("5", "5"))),
      InteractiveStudent("s4", "Петров С.", "11-А", mapOf("Геометрия" to listOf("4", "3", "3"), "Физика" to listOf("4", "2", "3"), "Литература" to listOf("2"))),
      InteractiveStudent("s5", "Смирнов А.", "11-А", mapOf("Геометрия" to listOf("5", "4", "4"), "Физика" to listOf("2", "4"), "Литература" to listOf("4")))
    )
  }

  // Custom Admin edited lessons schedule database map (Index of Edited Days)
  val schedulesDb = remember { mutableStateMapOf<String, List<ScheduleLesson>>() }

  // Currently resolved 7 items schedule for the selected date
  val resolvedLessonsList by remember {
    derivedStateOf {
      schedulesDb[dateKey] ?: generateDefaultWeekdayLessons(activeDayOfWeek, dateKey)
    }
  }

  // Active Homework Checkbox States
  var homeworkCompletedState by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }

  // Dialog States
  var activeGradeDetail by remember { mutableStateOf<GradeItemCard?>(null) }
  var showInfoQrDialog by remember { mutableStateOf(false) }

  // Edit Lesson Dialog State for Admins & Teachers
  var isEditingLessonDialogVisible by remember { mutableStateOf(false) }
  var lessonToEditIndex by remember { mutableIntStateOf(0) }
  var editLessonSubjectName by remember { mutableStateOf("") }
  var editLessonTimeRange by remember { mutableStateOf("") }
  var editLessonHomeworkText by remember { mutableStateOf("") }

  // Liquid Cosmic background animation loop (Liquid Glass Lava Lamp effects rendering)
  val infiniteTransition = rememberInfiniteTransition(label = "LiquidGlassBkg")
  val animOffset1 by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(12000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "blob1"
  )
  val animOffset2 by infiniteTransition.animateFloat(
    initialValue = 180f,
    targetValue = 540f,
    animationSpec = infiniteRepeatable(
      animation = tween(16000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "blob2"
  )

  Scaffold(
    containerColor = Color.Transparent, // Allows dynamic blurred glow canvas underneath
    modifier = Modifier.background(DiaryColors.Background),
    topBar = {
      CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = Color.Transparent,
          titleContentColor = DiaryColors.PrimaryText
        ),
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Rounded.MenuBook,
              contentDescription = "Дневник.Ру Logo Icon",
              tint = DiaryColors.AccentBlue,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Дневник.Ру",
              fontSize = 20.sp,
              fontWeight = FontWeight.Black,
              modifier = Modifier.testTag("app_title")
            )
          }
        },
        actions = {
          if (isUserLoggedIn) {
            IconButton(
              onClick = { selectedTab = 3 },
              modifier = Modifier.testTag("auth_shortcut_icon")
            ) {
              Icon(
                imageVector = Icons.Rounded.AdminPanelSettings,
                contentDescription = "Interactive Account Roles Panel",
                tint = DiaryColors.PrimaryText
              )
            }
          }
        }
      )
    },
    bottomBar = {
      if (isUserLoggedIn) {
        NavigationBar(
          containerColor = Color(0xCF1E293B), // Frosted navigation bar matching Liquid Glass
          tonalElevation = 1.dp,
          modifier = Modifier
            .shadow(8.dp)
            .navigationBarsPadding()
            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
          val tab0Label = when (currentRole) {
            UserRole.SENIOR_ADMIN, UserRole.JUNIOR_ADMIN -> "Панель Админа"
            UserRole.TEACHER -> "План уроков"
            UserRole.PARENT -> "Дневник ребенка"
            else -> "Дневник"
          }
          NavigationBarItem(
            selected = (selectedTab == 0),
            onClick = { selectedTab = 0 },
            icon = { 
              Icon(
                imageVector = when (currentRole) {
                  UserRole.SENIOR_ADMIN, UserRole.JUNIOR_ADMIN -> Icons.Rounded.AdminPanelSettings
                  UserRole.TEACHER -> Icons.Rounded.CoPresent
                  else -> Icons.Rounded.Home
                }, 
                contentDescription = "Diary Tab"
              )
            },
            label = { Text(tab0Label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = DiaryColors.AccentBlue,
              selectedTextColor = DiaryColors.AccentBlue,
              indicatorColor = Color(0x333B82F6),
              unselectedIconColor = DiaryColors.SecondaryText,
              unselectedTextColor = DiaryColors.SecondaryText
            )
          )

          if (currentRole != UserRole.SENIOR_ADMIN && currentRole != UserRole.JUNIOR_ADMIN) {
            val tab1Label = if (currentRole == UserRole.TEACHER) "Журнал" else "Сетка оценок"
            NavigationBarItem(
              selected = (selectedTab == 1),
              onClick = { selectedTab = 1 },
              icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Gradebook Grid Tab") },
              label = { Text(tab1Label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DiaryColors.AccentPurple,
                selectedTextColor = DiaryColors.AccentPurple,
                indicatorColor = Color(0x338B5CF6),
                unselectedIconColor = DiaryColors.SecondaryText,
                unselectedTextColor = DiaryColors.SecondaryText
              )
            )
          }

          if (currentRole == UserRole.STUDENT || currentRole == UserRole.PARENT) {
            NavigationBarItem(
              selected = (selectedTab == 2),
              onClick = { selectedTab = 2 },
              icon = { Icon(Icons.Rounded.BarChart, contentDescription = "School Statistics Tab") },
              label = { Text("Рейтинги", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DiaryColors.AccentGreen,
                selectedTextColor = DiaryColors.AccentGreen,
                indicatorColor = Color(0x3310B981),
                unselectedIconColor = DiaryColors.SecondaryText,
                unselectedTextColor = DiaryColors.SecondaryText
              )
            )
          }

          NavigationBarItem(
            selected = (selectedTab == 3),
            onClick = { selectedTab = 3 },
            icon = { Icon(Icons.Rounded.Person, contentDescription = "Profile Tab") },
            label = { Text("Профиль", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = DiaryColors.AccentBlue,
              selectedTextColor = DiaryColors.AccentBlue,
              indicatorColor = Color(0x333B82F6),
              unselectedIconColor = DiaryColors.SecondaryText,
              unselectedTextColor = DiaryColors.SecondaryText
            )
          )
        }
      }
    }
  ) { masterPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(masterPadding)
    ) {
      // Animated lava lamp liquid circles under glass background
      Canvas(modifier = Modifier.fillMaxSize().blur(80.dp)) {
        val cos1 = Math.cos(Math.toRadians(animOffset1.toDouble()))
        val sin1 = Math.sin(Math.toRadians(animOffset1.toDouble()))
        val cos2 = Math.cos(Math.toRadians(animOffset2.toDouble()))
        val sin2 = Math.sin(Math.toRadians(animOffset2.toDouble()))

        // Soft floating orbital glow center 1 (Indigo Core)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF312E81), Color(0x000F172A)),
            center = Offset(
              x = (size.width * 0.4f + cos1 * size.width * 0.15f).toFloat(),
              y = (size.height * 0.3f + sin1 * size.height * 0.1f).toFloat()
            ),
            radius = size.width * 0.5f
          )
        )

        // Soft floating orbital glow center 2 (Deep Violet/Beetroot)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF4C1D95), Color(0x000F172A)),
            center = Offset(
              x = (size.width * 0.6f + cos2 * size.width * 0.12f).toFloat(),
              y = (size.height * 0.7f + sin2 * size.height * 0.15f).toFloat()
            ),
            radius = size.width * 0.45f
          )
        )
      }

      // Render content depending on authorization check
      if (!isUserLoggedIn) {
        FullScreenLoginView(
          onRoleChange = { newRole, login ->
            currentRole = newRole
            currentUserLogin = login
            isUserLoggedIn = true
            selectedTab = 0 // Reset to main diary tab
          },
          credentialDatabaseList = listOf(
            MockUserCred("senior_admin", "1234", UserRole.SENIOR_ADMIN),
            MockUserCred("junior_admin", "5678", UserRole.JUNIOR_ADMIN),
            MockUserCred("teacher_math", "math123", UserRole.TEACHER),
            MockUserCred("parent_user", "parent99", UserRole.PARENT),
            MockUserCred("student_user", "user432", UserRole.STUDENT)
          )
        )
      } else {
        when (selectedTab) {
          0 -> {
            if (currentRole == UserRole.SENIOR_ADMIN || currentRole == UserRole.JUNIOR_ADMIN) {
              AdminDiaryWorkspace(
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                selectedDay = selectedDay,
                activeDayOfWeek = activeDayOfWeek,
                activeDayStatus = activeDayStatus,
                onYearChange = { selectedYear = it },
                onMonthChange = { selectedMonth = it },
                onDayChange = { selectedDay = it },
                studentDatabase = studentDatabase,
                resolvedLessonsList = resolvedLessonsList,
                onTriggerEditLesson = { index, lesson ->
                  lessonToEditIndex = index
                  editLessonSubjectName = lesson.subject
                  editLessonTimeRange = lesson.time
                  editLessonHomeworkText = lesson.homeworkPlaceholder
                  isEditingLessonDialogVisible = true
                },
                currentRole = currentRole,
                onPromoClick = { showInfoQrDialog = true }
              )
            } else {
              MainDiaryTabContent(
                currentRole = currentRole,
                importantTitle = importantTitle,
                importantContent = importantContent,
                resolvedLessonsList = resolvedLessonsList,
                studentDatabase = studentDatabase,
                activeDayStatus = activeDayStatus,
                activeDayOfWeek = activeDayOfWeek,
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                selectedDay = selectedDay,
                onPromoClick = { showInfoQrDialog = true },
                onGradeClick = { activeGradeDetail = it },
                homeworkCompletedState = homeworkCompletedState,
                onToggleHomework = { lessonNum ->
                  val key = "$dateKey-$lessonNum"
                  val isChecked = homeworkCompletedState[key] ?: false
                  homeworkCompletedState = homeworkCompletedState.toMutableMap().apply {
                    put(key, !isChecked)
                  }
                },
                // Dropdowns for interactive Year (2026-2030), Month and Day selection
                onYearChange = { selectedYear = it },
                onMonthChange = { selectedMonth = it },
                onDayChange = { selectedDay = it },
                onTriggerEditLesson = { index, lesson ->
                  lessonToEditIndex = index
                  editLessonSubjectName = lesson.subject
                  editLessonTimeRange = lesson.time
                  editLessonHomeworkText = lesson.homeworkPlaceholder
                  isEditingLessonDialogVisible = true
                }
              )
            }
          }
          1 -> SecondaryGradesTabContent(
            currentRole = currentRole,
            studentDatabase = studentDatabase,
            resolvedLessonsList = resolvedLessonsList,
            onAddGradeToStudent = { studentIndex, subject, grade ->
              if (studentIndex in studentDatabase.indices) {
                val currentStudent = studentDatabase[studentIndex]
                val currentGradesForSubject = currentStudent.grades[subject]?.toMutableList() ?: mutableListOf()
                currentGradesForSubject.add(grade)

                val updatedMap = currentStudent.grades.toMutableMap()
                updatedMap[subject] = currentGradesForSubject

                studentDatabase[studentIndex] = currentStudent.copy(grades = updatedMap)
              }
            },
            // Administrators can overwrite homework directly for lessons active on selected date
            onAssignHomeworkTask = { index, targetSubject, homeworkText ->
              val mutableSchedule = resolvedLessonsList.toMutableList()
              if (index in mutableSchedule.indices) {
                mutableSchedule[index] = mutableSchedule[index].copy(homeworkPlaceholder = homeworkText)
                schedulesDb[dateKey] = mutableSchedule
              }
            }
          )
          2 -> SchoolStatsTabContent()
          3 -> CabinetTabContent(
            currentRole = currentRole,
            currentUserLogin = currentUserLogin,
            isLoggedIn = isUserLoggedIn,
            onRoleChange = { newRole, login ->
              currentRole = newRole
              currentUserLogin = login
              isUserLoggedIn = true
            },
            onLogout = {
              isUserLoggedIn = false
            },
            onUpdateAnnouncement = { title, content ->
              importantTitle = title
              importantContent = content
            }
          )
        }
      }
    }
  }

  // Edit Lesson dialog for Admins & Teachers (Adjust anything)
  if (isEditingLessonDialogVisible) {
    Dialog(onDismissRequest = { isEditingLessonDialogVisible = false }) {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFA1E293B)),
        border = BorderStroke(1.dp, DiaryColors.GlassBorder),
        modifier = Modifier.padding(16.dp).fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Text(
            text = "Редактировать Урок ${lessonToEditIndex + 1}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = editLessonSubjectName,
            onValueChange = { editLessonSubjectName = it },
            label = { Text("Название предмета") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedLabelColor = DiaryColors.AccentBlue
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editLessonTimeRange,
            onValueChange = { editLessonTimeRange = it },
            label = { Text("Время проведения") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedLabelColor = DiaryColors.AccentBlue
            ),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = editLessonHomeworkText,
            onValueChange = { editLessonHomeworkText = it },
            label = { Text("Заданное домашнее задание") },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedLabelColor = DiaryColors.AccentBlue
            ),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
          )

          Spacer(modifier = Modifier.height(18.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            TextButton(onClick = { isEditingLessonDialogVisible = false }) {
              Text("Отмена", color = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
              onClick = {
                val mutableSchedule = resolvedLessonsList.toMutableList()
                if (lessonToEditIndex in mutableSchedule.indices) {
                  val old = mutableSchedule[lessonToEditIndex]
                  mutableSchedule[lessonToEditIndex] = old.copy(
                    subject = editLessonSubjectName,
                    time = editLessonTimeRange,
                    homeworkPlaceholder = editLessonHomeworkText
                  )
                  schedulesDb[dateKey] = mutableSchedule
                }
                isEditingLessonDialogVisible = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = DiaryColors.AccentPurple)
            ) {
              Text("Сохранить", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  if (activeGradeDetail != null) {
    BeautifulGradeDetailDialog(
      item = activeGradeDetail!!,
      onDismiss = { activeGradeDetail = null }
    )
  }

  if (showInfoQrDialog) {
    QrPromotionInfoDialog(onDismiss = { showInfoQrDialog = false })
  }
}

// -------------------------------------------------------------
// GLASSMORPHIC CARD COMPONENT
// -------------------------------------------------------------
@Composable
fun GlassyCard(
  modifier: Modifier = Modifier,
  borderAlpha: Float = 0.25f,
  bgAlpha: Float = 0.55f,
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    modifier = modifier.shadow(2.dp, RoundedCornerShape(20.dp), clip = false),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFF1E293B).copy(alpha = bgAlpha)
    ),
    border = BorderStroke(1.dp, Color.White.copy(alpha = borderAlpha))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      content()
    }
  }
}

// -------------------------------------------------------------
// TAB 0: DIARY CONTENT RENDERING FOR ADMINISTRATION
// -------------------------------------------------------------
@Composable
fun AdminDiaryWorkspace(
  selectedYear: Int,
  selectedMonth: Int,
  selectedDay: Int,
  activeDayOfWeek: Int,
  activeDayStatus: DayStatus,
  onYearChange: (Int) -> Unit,
  onMonthChange: (Int) -> Unit,
  onDayChange: (Int) -> Unit,
  studentDatabase: List<InteractiveStudent>,
  resolvedLessonsList: List<ScheduleLesson>,
  onTriggerEditLesson: (Int, ScheduleLesson) -> Unit,
  currentRole: UserRole,
  onPromoClick: () -> Unit
) {
  var selectedStudentIndex by remember { mutableStateOf(0) }
  val targetStudent = studentDatabase.getOrNull(selectedStudentIndex) ?: studentDatabase.first()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
  ) {
    item {
      ProfileCardHeader(currentRole = currentRole)
    }

    item {
      RoleContextWelcomeCard(currentRole = currentRole)
    }

    item {
      CalendarDashboardControlWidget(
        selectedYear = selectedYear,
        selectedMonth = selectedMonth,
        selectedDay = selectedDay,
        activeDayOfWeek = activeDayOfWeek,
        activeDayStatus = activeDayStatus,
        onYearSelected = onYearChange,
        onMonthSelected = onMonthChange,
        onDaySelected = onDayChange
      )
    }

    // Interactive Student Selector
    item {
      GlassyCard(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "👤 Реестр обучающихся школы",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = "Выберите ученика для проверки заданий и успеваемости:",
          fontSize = 11.sp,
          color = DiaryColors.SecondaryText
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          studentDatabase.forEachIndexed { idx, student ->
            val isSelected = idx == selectedStudentIndex
            Card(
              modifier = Modifier
                .width(135.dp)
                .clickable { selectedStudentIndex = idx },
              colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0x3310B981) else Color(0x15FFFFFF)
              ),
              border = BorderStroke(
                width = 1.dp,
                color = if (isSelected) DiaryColors.AccentGreen else Color(0x11FFFFFF)
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = student.name,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = "Класс: ${student.gradeClass}",
                  fontSize = 10.sp,
                  color = DiaryColors.SecondaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x3310B981))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "Активен",
                    fontSize = 8.sp,
                    color = DiaryColors.AccentGreen,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }
    }

    // Selected student schedule showing list of lessons and homework text
    item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Rounded.Assignment,
            contentDescription = "homework symbol icon",
            tint = DiaryColors.AccentBlue,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Д/З и уроки ученика: ${targetStudent.name}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }

    if (activeDayStatus.isVacation) {
      item {
        VacationHolidayHeroCard(status = activeDayStatus)
      }
    } else if (activeDayOfWeek == 7) {
      item {
        WeekendRelaxCard()
      }
    } else {
      items(resolvedLessonsList.take(7)) { lesson ->
        val itemIndex = resolvedLessonsList.indexOf(lesson)
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
          border = BorderStroke(1.dp, Color(0x11FFFFFF))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.width(80.dp)) {
              Text(
                text = lesson.time,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = DiaryColors.SecondaryText
              )
              Text(
                text = lesson.lessonNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = DiaryColors.AccentBlue
              )
            }

            Box(
              modifier = Modifier
                .height(34.dp)
                .width(1.dp)
                .background(Color(0x22FFFFFF))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = lesson.subject,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Задание: ${lesson.homeworkPlaceholder}",
                fontSize = 11.sp,
                color = DiaryColors.SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            // Edit Button for Admin to edit user's lesson
            IconButton(
              onClick = { onTriggerEditLesson(itemIndex, lesson) },
              modifier = Modifier.size(34.dp)
            ) {
              Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Корректировка урока администрацией",
                tint = DiaryColors.AccentPurple
              )
            }
          }
        }
      }
    }

    // Displaying Selected Student's Grades to the Admin since they administer them
    item {
      GlassyCard(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "📈 Сводные оценки ученика по предметам",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "Как администратор, вы можете видеть успеваемость учеников:",
          fontSize = 11.sp,
          color = DiaryColors.SecondaryText
        )
        Spacer(modifier = Modifier.height(10.dp))

        val subjects = listOf("Геометрия", "Физика", "Литература")
        subjects.forEach { subject ->
          val gradesForThis = targetStudent.grades[subject] ?: emptyList()
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = subject,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.width(90.dp)
            )

            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.weight(1f)
            ) {
              gradesForThis.forEach { singleGrade ->
                val colP = getNumericalGradeColor(singleGrade)
                val colB = getNumericalGradeBgColor(singleGrade)
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colB),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = singleGrade,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colP
                  )
                }
              }
            }

            val avg = if (gradesForThis.isNotEmpty()) {
              "%.2f".format(gradesForThis.mapNotNull { it.toIntOrNull() }.average())
            } else "0.00"
            Text(
              text = "Ср: $avg",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = DiaryColors.AccentGreen
            )
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 0: DIARY CONTENT RENDERING
// -------------------------------------------------------------
@Composable
fun MainDiaryTabContent(
  currentRole: UserRole,
  importantTitle: String,
  importantContent: String,
  resolvedLessonsList: List<ScheduleLesson>,
  studentDatabase: List<InteractiveStudent>,
  activeDayStatus: DayStatus,
  activeDayOfWeek: Int,
  selectedYear: Int,
  selectedMonth: Int,
  selectedDay: Int,
  onPromoClick: () -> Unit,
  onGradeClick: (GradeItemCard) -> Unit,
  homeworkCompletedState: Map<String, Boolean>,
  onToggleHomework: (String) -> Unit,
  onYearChange: (Int) -> Unit,
  onMonthChange: (Int) -> Unit,
  onDayChange: (Int) -> Unit,
  onTriggerEditLesson: (Int, ScheduleLesson) -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
  ) {
    item {
      ProfileCardHeader(currentRole = currentRole)
    }

    item {
      RoleContextWelcomeCard(currentRole = currentRole)
    }

    // Interactive custom calendar dashboard widget
    item {
      CalendarDashboardControlWidget(
        selectedYear = selectedYear,
        selectedMonth = selectedMonth,
        selectedDay = selectedDay,
        activeDayOfWeek = activeDayOfWeek,
        activeDayStatus = activeDayStatus,
        onYearSelected = onYearChange,
        onMonthSelected = onMonthChange,
        onDaySelected = onDayChange
      )
    }

    item {
      ImportantSectionView(title = importantTitle, content = importantContent)
    }

    item {
      PromoQrBannerView(onDetailsClick = onPromoClick)
    }

    if (currentRole == UserRole.STUDENT || currentRole == UserRole.PARENT) {
      item {
        SchoolAttendanceStatusCard()
      }
    }

    item {
      RecentGradesBlockSection(
        currentRole = currentRole,
        studentDatabase = studentDatabase,
        onGradeClick = onGradeClick
      )
    }

    item {
      ScheduleSectionHeaderView(resolvedLessonsList = resolvedLessonsList)
    }

    // Calendar check: Weekend (Su=7, Sa=6) or school vacations/holidays
    if (activeDayStatus.isVacation) {
      item {
        VacationHolidayHeroCard(status = activeDayStatus)
      }
    } else if (activeDayOfWeek == 7) {
      item {
        WeekendRelaxCard()
      }
    } else {
      // Normal school day - Exactly 7 lessons!
      items(resolvedLessonsList.take(7)) { lesson ->
        val itemIndex = resolvedLessonsList.indexOf(lesson)
        val key = "%04d-%02d-%02d-%s".format(selectedYear, selectedMonth, selectedDay, lesson.lessonNumber)
        val isDone = homeworkCompletedState[key] ?: false
        
        LessonRowItem(
          lesson = lesson,
          isCompleted = isDone,
          onToggleStatus = { onToggleHomework(lesson.lessonNumber) },
          currentRole = currentRole,
          onEditClick = { onTriggerEditLesson(itemIndex, lesson) }
        )
      }
    }
  }
}

@Composable
fun ProfileCardHeader(currentRole: UserRole) {
  GlassyCard(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(54.dp)
          .clip(CircleShape)
          .background(currentRole.badgeColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = when (currentRole) {
            UserRole.SENIOR_ADMIN, UserRole.JUNIOR_ADMIN -> Icons.Rounded.AdminPanelSettings
            UserRole.TEACHER -> Icons.Rounded.CoPresent
            UserRole.PARENT -> Icons.Rounded.Group
            UserRole.STUDENT -> Icons.Rounded.Person
          },
          contentDescription = "User Mode Icon",
          tint = currentRole.titleColor,
          modifier = Modifier.size(28.dp)
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = when (currentRole) {
            UserRole.SENIOR_ADMIN -> "Главный Администратор (Школа РФ)"
            UserRole.JUNIOR_ADMIN -> "Мл. Администратор (Школа РФ)"
            UserRole.TEACHER -> "Учитель Семёнова Е.В."
            UserRole.PARENT -> "Родитель Спиридонова Р.С."
            UserRole.STUDENT -> "Имя ученика S. (Имя S.)"
          },
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = DiaryColors.PrimaryText
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.Apartment,
            contentDescription = "School label",
            tint = DiaryColors.SecondaryText,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "ГБОУ СОШ №145 (Проверен оффлайн)",
            fontSize = 12.sp,
            color = DiaryColors.SecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

@Composable
fun RoleContextWelcomeCard(currentRole: UserRole) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(currentRole.badgeColor)
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Icons.Rounded.Info,
        contentDescription = "Current Role Identifier",
        tint = currentRole.titleColor,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Вы вошли как: ${currentRole.displayName}",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = currentRole.titleColor
      )
    }
  }
}

// -------------------------------------------------------------
// DYNAMIC INTERACTIVE CALENDAR ENGINE GRAPHICAL CONTROL CONTAINER
// -------------------------------------------------------------
@Composable
fun CalendarDashboardControlWidget(
  selectedYear: Int,
  selectedMonth: Int,
  selectedDay: Int,
  activeDayOfWeek: Int,
  activeDayStatus: DayStatus,
  onYearSelected: (Int) -> Unit,
  onMonthSelected: (Int) -> Unit,
  onDaySelected: (Int) -> Unit
) {
  var isCalendarMenuExpanded by remember { mutableStateOf(false) }

  GlassyCard(modifier = Modifier.fillMaxWidth()) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Rounded.CalendarMonth,
              contentDescription = "Calendar view",
              tint = DiaryColors.AccentPurple,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Школьный Календарь",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
          // Displaying dynamic Russian translation format of the selected target date
          Text(
            text = "%02d %s %d год (%s)".format(
              selectedDay,
              RussianMonths[selectedMonth - 1],
              selectedYear,
              getRussianDayOfWeekName(activeDayOfWeek)
            ),
            fontSize = 12.sp,
            color = DiaryColors.SecondaryText,
            fontWeight = FontWeight.Medium
          )
        }

        IconButton(
          onClick = { isCalendarMenuExpanded = !isCalendarMenuExpanded },
          modifier = Modifier.background(Color(0x33FFFFFF), CircleShape)
        ) {
          Icon(
            imageVector = if (isCalendarMenuExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
            contentDescription = "Configure Date Parameter Settings",
            tint = Color.White
          )
        }
      }

      AnimatedVisibility(visible = isCalendarMenuExpanded) {
        Column(modifier = Modifier.padding(top = 12.dp)) {
          Divider(color = Color(0x22FFFFFF), modifier = Modifier.padding(bottom = 10.dp))

          // 1. Selector of parameters (Year limit 2026-2030)
          Text("Год обучения:", fontSize = 11.sp, color = DiaryColors.SecondaryText, fontWeight = FontWeight.Bold)
          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            (2026..2030).forEach { yearOption ->
              val isSelected = yearOption == selectedYear
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) DiaryColors.AccentPurple else Color(0x15FFFFFF))
                  .clickable {
                    onYearSelected(yearOption)
                    // Bound-check selected day on month sizing limits
                    val maxDays = getDaysInMonth(selectedMonth, yearOption)
                    if (selectedDay > maxDays) onDaySelected(maxDays)
                  }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = "$yearOption г.",
                  fontSize = 12.sp,
                  color = Color.White,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // 2. Month Selector
          Text("Месяц учебного года:", fontSize = 11.sp, color = DiaryColors.SecondaryText, fontWeight = FontWeight.Bold)
          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            RussianMonths.forEachIndexed { idx, monthOption ->
              val mIndex = idx + 1
              val isSelected = mIndex == selectedMonth
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) DiaryColors.AccentBlue else Color(0x15FFFFFF))
                  .clickable {
                    onMonthSelected(mIndex)
                    val maxDays = getDaysInMonth(mIndex, selectedYear)
                    if (selectedDay > maxDays) onDaySelected(maxDays)
                  }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = monthOption,
                  fontSize = 12.sp,
                  color = Color.White,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // 3. Grid of Days
          val maxDaysInCurrentMonth = getDaysInMonth(selectedMonth, selectedYear)
          Text(
            text = "День месяца (доступно 1..$maxDaysInCurrentMonth):",
            fontSize = 11.sp,
            color = DiaryColors.SecondaryText,
            fontWeight = FontWeight.Bold
          )
          
          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            (1..maxDaysInCurrentMonth).forEach { dayOption ->
              val isSelected = dayOption == selectedDay
              val currentDayStatus = detectDayStatus(dayOption, selectedMonth, selectedYear)
              val dOfWeek = getDayOfWeek(dayOption, selectedMonth, selectedYear)

              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(
                    if (isSelected) DiaryColors.AccentGreen
                    else if (currentDayStatus.isHoliday) DiaryColors.RedAccentLight
                    else if (currentDayStatus.isVacation) DiaryColors.OrangeAccentLight
                    else if (dOfWeek == 7) Color(0x11FFFFFF)
                    else Color(0x22FFFFFF)
                  )
                  .border(
                    width = 1.dp,
                    color = if (isSelected) Color.White else Color.Transparent,
                    shape = CircleShape
                  )
                  .clickable { onDaySelected(dayOption) },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "$dayOption",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else if (currentDayStatus.isHoliday) DiaryColors.AccentRed else Color.White
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Status Indicator Pill for Selected Day
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(
            if (activeDayStatus.isHoliday) DiaryColors.RedAccentLight
            else if (activeDayStatus.isVacation) DiaryColors.OrangeAccentLight
            else if (activeDayOfWeek == 7) Color(0x15FFFFFF)
            else DiaryColors.GreenAccentLight
          )
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(
                if (activeDayStatus.isHoliday) DiaryColors.AccentRed
                else if (activeDayStatus.isVacation) DiaryColors.AccentOrange
                else if (activeDayOfWeek == 7) DiaryColors.SecondaryText
                else DiaryColors.AccentGreen
              )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Статус на выбранную дату: ${activeDayStatus.statusName}",
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

// Visual screen replacements for Vacations & Holidays
@Composable
fun VacationHolidayHeroCard(status: DayStatus) {
  GlassyCard(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(DiaryColors.OrangeAccentLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Rounded.School,
          contentDescription = "Vacation Icon representation",
          tint = DiaryColors.AccentOrange,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = status.statusName,
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        color = Color.White,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Период каникул или официальных праздников ГБОУ.\nЗанятия по расписанию не проводятся! 🎉",
        fontSize = 12.sp,
        color = DiaryColors.SecondaryText,
        textAlign = TextAlign.Center,
        lineHeight = 16.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }
  }
}

@Composable
fun WeekendRelaxCard() {
  GlassyCard(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(DiaryColors.BlueAccentLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Rounded.WbSunny,
          contentDescription = "Weekend Icon Sun",
          tint = DiaryColors.AccentBlue,
          modifier = Modifier.size(36.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Отличных выходных! ⛱️",
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        color = Color.White,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Школа временно закрыта на уикенд.\nПроведайте близких, отдохните от уроков!",
        fontSize = 12.sp,
        color = DiaryColors.SecondaryText,
        textAlign = TextAlign.Center,
        lineHeight = 16.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
    }
  }
}

@Composable
fun ImportantSectionView(title: String, content: String) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(1.dp, RoundedCornerShape(16.dp), clip = false)
      .testTag("important_section"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DiaryColors.WarningAccentLight),
    border = BorderStroke(1.dp, Color(0x33F59E0B))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.Top
    ) {
      Icon(
        imageVector = Icons.Rounded.NotificationImportant,
        contentDescription = "Notification alert element",
        tint = DiaryColors.WarningPrimary,
        modifier = Modifier
          .size(24.dp)
          .padding(top = 2.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = title,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = content,
          fontSize = 12.sp,
          color = DiaryColors.SecondaryText,
          lineHeight = 16.sp
        )
      }
    }
  }
}

@Composable
fun PromoQrBannerView(onDetailsClick: () -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(2.dp, RoundedCornerShape(16.dp), clip = false)
      .testTag("promo_banner"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DiaryColors.BlueAccentLight),
    border = BorderStroke(1.dp, Color(0x553B82F6))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Rounded.QrCode2,
          contentDescription = "App login representation",
          tint = DiaryColors.AccentBlue,
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Безопасный вход по QR",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(1.dp))
          Text(
            text = "Мгновенное подключение к дневнику",
            fontSize = 11.sp,
            color = DiaryColors.SecondaryText
          )
        }
      }

      Button(
        onClick = onDetailsClick,
        colors = ButtonDefaults.buttonColors(containerColor = DiaryColors.AccentBlue),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.defaultMinSize(minHeight = 32.dp)
      ) {
        Text(
          text = "Вход",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}

@Composable
fun SchoolAttendanceStatusCard() {
  GlassyCard(modifier = Modifier.fillMaxWidth().testTag("attendance_card")) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Rounded.VerifiedUser,
            contentDescription = "Checked attendance label",
            tint = DiaryColors.AccentGreen,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Внутренний статус посещаемости",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DiaryColors.GreenAccentLight)
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = "Оффлайн",
            fontSize = 10.sp,
            color = DiaryColors.AccentGreen,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0x33000000), RoundedCornerShape(12.dp))
          .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Режим работы: Стабильный автономный",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Учёт каникул и праздников автоматизирован до 2030 года",
            fontSize = 11.sp,
            color = DiaryColors.SecondaryText
          )
        }

        Text(
          text = "N",
          fontSize = 28.sp,
          fontWeight = FontWeight.Black,
          color = DiaryColors.SecondaryText,
          modifier = Modifier.padding(end = 4.dp)
        )
      }
    }
  }
}

@Composable
fun RecentGradesBlockSection(
  currentRole: UserRole,
  studentDatabase: List<InteractiveStudent>,
  onGradeClick: (GradeItemCard) -> Unit
) {
  if (currentRole == UserRole.TEACHER || currentRole == UserRole.SENIOR_ADMIN || currentRole == UserRole.JUNIOR_ADMIN) {
    return
  }
  val sampleGradesList = remember(studentDatabase) {
    listOf(
      GradeItemCard("5", "Алгебра", "Контр. работа", "22 Май"),
      GradeItemCard("4", "Физика", "Ответ у доски", "21 Май"),
      GradeItemCard("3", "Геометрия", "Домашняя работа", "19 Май"),
      GradeItemCard("2", "Химия", "Тест по элементам", "18 Май"),
      GradeItemCard("4", "Литература", "Устное сочинение", "15 Май")
    )
  }

  Column(modifier = Modifier.fillMaxWidth().testTag("recent_grades")) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.Assignment,
          contentDescription = "Academics Symbol",
          tint = DiaryColors.AccentBlue,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (currentRole == UserRole.PARENT) "Последние оценки ребенка (Алексеев М.)" else "Последние Оценки",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
      Text(
        text = "Подробности на тап",
        fontSize = 11.sp,
        color = DiaryColors.SecondaryText,
        fontWeight = FontWeight.Medium
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      sampleGradesList.forEach { item ->
        Card(
          modifier = Modifier
            .width(118.dp)
            .clickable { onGradeClick(item) },
          colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
          border = BorderStroke(1.dp, Color(0x15FFFFFF)),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            val colPrimary = getNumericalGradeColor(item.gradeValue)
            val colBg = getNumericalGradeBgColor(item.gradeValue)

            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colBg),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = item.gradeValue,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colPrimary
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = item.subjectName,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
              text = item.testType,
              fontSize = 10.sp,
              color = DiaryColors.SecondaryText,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
              text = item.examDate,
              fontSize = 9.sp,
              fontWeight = FontWeight.Medium,
              color = DiaryColors.SecondaryText,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }
  }
}

fun getNumericalGradeColor(grade: String): Color {
  return when (grade) {
    "5" -> DiaryColors.AccentGreen
    "4" -> DiaryColors.AccentOrange
    "3" -> DiaryColors.AccentPurple
    "2" -> DiaryColors.AccentRed
    else -> DiaryColors.SecondaryText
  }
}

fun getNumericalGradeBgColor(grade: String): Color {
  return when (grade) {
    "5" -> DiaryColors.GreenAccentLight
    "4" -> DiaryColors.OrangeAccentLight
    "3" -> DiaryColors.PurpleAccentLight
    "2" -> DiaryColors.RedAccentLight
    else -> Color(0x22FFFFFF)
  }
}

data class GradeItemCard(
  val gradeValue: String,
  val subjectName: String,
  val testType: String,
  val examDate: String
)

@Composable
fun ScheduleSectionHeaderView(resolvedLessonsList: List<ScheduleLesson>) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Icons.Rounded.Today,
        contentDescription = "Current schedule calendar emblem",
        tint = Color.White,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "Дневное расписание уроков (7 слотов)",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(DiaryColors.BlueAccentLight)
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = "7/7",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = DiaryColors.AccentBlue
        )
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(DiaryColors.PurpleAccentLight)
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = "Д/З активно",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = DiaryColors.AccentPurple
        )
      }
    }
  }
}

@Composable
fun LessonRowItem(
  lesson: ScheduleLesson,
  isCompleted: Boolean,
  onToggleStatus: () -> Unit,
  currentRole: UserRole,
  onEditClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
    border = BorderStroke(1.dp, Color(0x11FFFFFF))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.width(80.dp)) {
        Text(
          text = lesson.time,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = DiaryColors.SecondaryText
        )
        Text(
          text = lesson.lessonNumber,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = DiaryColors.AccentBlue
        )
      }

      Box(
        modifier = Modifier
          .height(34.dp)
          .width(1.dp)
          .background(Color(0x22FFFFFF))
      )

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = lesson.subject,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "Задание: ${lesson.homeworkPlaceholder}",
          fontSize = 11.sp,
          color = if (isCompleted) DiaryColors.AccentGreen else DiaryColors.SecondaryText,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      // If user is Admin or Teacher: Show administrative editing configuration button!
      if (currentRole == UserRole.TEACHER || currentRole == UserRole.SENIOR_ADMIN || currentRole == UserRole.JUNIOR_ADMIN) {
        IconButton(
          onClick = onEditClick,
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Корректировка урока администрацией",
            tint = DiaryColors.AccentPurple
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
      }

      if (currentRole == UserRole.STUDENT) {
        IconButton(
          onClick = onToggleStatus,
          modifier = Modifier.size(34.dp)
        ) {
          Icon(
            imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Outlined.CheckCircle,
            contentDescription = "Tick completed homework status",
            tint = if (isCompleted) DiaryColors.AccentGreen else DiaryColors.SecondaryText
          )
        }
      } else if (currentRole == UserRole.PARENT) {
        Box(
          modifier = Modifier.size(34.dp).padding(5.dp),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isCompleted) Icons.Rounded.Verified else Icons.Outlined.CheckCircle,
            contentDescription = "Homework completed status by student",
            tint = if (isCompleted) DiaryColors.AccentGreen else DiaryColors.SecondaryText,
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 1: GRADES GRID & WORKSPACE
// -------------------------------------------------------------
@Composable
fun SecondaryGradesTabContent(
  currentRole: UserRole,
  studentDatabase: List<InteractiveStudent>,
  resolvedLessonsList: List<ScheduleLesson>,
  onAddGradeToStudent: (Int, String, String) -> Unit,
  onAssignHomeworkTask: (Int, String, String) -> Unit
) {
  var selectedStudentIndex by remember { mutableIntStateOf(0) }
  var selectedSubject by remember { mutableStateOf("Геометрия") }
  
  var homeworkTextToAssign by remember { mutableStateOf("") }
  var statusMessage by remember { mutableStateOf("") }

  val subjects = listOf("Геометрия", "Физика", "Литература")

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
  ) {
    item {
      GlassyCard(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Автономная Сетка Оценок & Заданий",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = DiaryColors.AccentBlue
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Интерактивный оффлайн-функционал для мгновенного управления оценками в ведомости класса.",
          fontSize = 11.sp,
          color = DiaryColors.SecondaryText
        )
      }
    }

    item {
      if (currentRole == UserRole.TEACHER || currentRole == UserRole.SENIOR_ADMIN || currentRole == UserRole.JUNIOR_ADMIN) {
        TeacherGridControlCard(
          studentDatabase = studentDatabase,
          subjects = subjects,
          resolvedLessonsList = resolvedLessonsList,
          selectedStudentIndex = selectedStudentIndex,
          selectedSubject = selectedSubject,
          homeworkTextToAssign = homeworkTextToAssign,
          statusMessage = statusMessage,
          onStudentSelect = { selectedStudentIndex = it },
          onSubjectSelect = { selectedSubject = it },
          onHomeworkTextChange = { homeworkTextToAssign = it },
          onAssignHomework = { lessonIdx ->
            onAssignHomeworkTask(lessonIdx, selectedSubject, homeworkTextToAssign)
            statusMessage = "Задание по предмету '$selectedSubject' зафиксировано в уроке №${lessonIdx + 1}!"
            homeworkTextToAssign = ""
          },
          onAddGrade = { assignedGrade ->
            onAddGradeToStudent(selectedStudentIndex, selectedSubject, assignedGrade)
            val name = studentDatabase.getOrNull(selectedStudentIndex)?.name ?: ""
            statusMessage = "Оценка [$assignedGrade] выставлена ученику $name по предмету $selectedSubject!"
          }
        )
      } else {
        StudentGradesViewCard(
          currentRole = currentRole,
          studentDatabase = studentDatabase
        )
      }
    }

    item {
      GradeOverviewMatrixSection(studentDatabase = studentDatabase, subjects = subjects)
    }
  }
}

@Composable
fun TeacherGridControlCard(
  studentDatabase: List<InteractiveStudent>,
  subjects: List<String>,
  resolvedLessonsList: List<ScheduleLesson>,
  selectedStudentIndex: Int,
  selectedSubject: String,
  homeworkTextToAssign: String,
  statusMessage: String,
  onStudentSelect: (Int) -> Unit,
  onSubjectSelect: (String) -> Unit,
  onHomeworkTextChange: (String) -> Unit,
  onAssignHomework: (Int) -> Unit,
  onAddGrade: (String) -> Unit
) {
  GlassyCard(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "Оффлайн консоль выставления оценок",
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text("Выбор Ученика класса:", fontSize = 11.sp, color = DiaryColors.SecondaryText, fontWeight = FontWeight.Bold)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      studentDatabase.forEachIndexed { index, student ->
        val isSel = selectedStudentIndex == index
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSel) Color(0x558B5CF6) else Color(0x22FFFFFF))
            .border(1.dp, if (isSel) DiaryColors.AccentPurple else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onStudentSelect(index) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = "${student.name} (${student.gradeClass})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text("Выбор Предмета:", fontSize = 11.sp, color = DiaryColors.SecondaryText, fontWeight = FontWeight.Bold)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      subjects.forEach { subj ->
        val isSel = selectedSubject == subj
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSel) Color(0x553B82F6) else Color(0x22FFFFFF))
            .border(1.dp, if (isSel) DiaryColors.AccentBlue else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onSubjectSelect(subj) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = subj,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text("Выставить отметку за выбранное число:", fontSize = 11.sp, color = DiaryColors.SecondaryText, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf("5", "4", "3", "2").forEach { valGrade ->
        val cPrimary = getNumericalGradeColor(valGrade)
        val cBg = getNumericalGradeBgColor(valGrade)
        
        Button(
          onClick = { onAddGrade(valGrade) },
          colors = ButtonDefaults.buttonColors(containerColor = cBg, contentColor = cPrimary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.weight(1f).height(44.dp),
          border = BorderStroke(1.dp, cPrimary)
        ) {
          Text(text = valGrade, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text("Задать быстро Д/З на выбранную дату:", fontSize = 11.sp, color = DiaryColors.SecondaryText, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = homeworkTextToAssign,
        onValueChange = onHomeworkTextChange,
        placeholder = { Text("Текст домашнего урока...") },
        maxLines = 1,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.weight(1f).height(48.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedBorderColor = DiaryColors.AccentBlue,
          unfocusedBorderColor = DiaryColors.BorderLight
        )
      )
      Spacer(modifier = Modifier.width(8.dp))
      Button(
        onClick = {
          // Find first lesson for this subject in the 7 slots
          val targetIdx = resolvedLessonsList.indexOfFirst { it.subject.lowercase().contains(selectedSubject.lowercase()) }
          onAssignHomework(if (targetIdx != -1) targetIdx else 0)
        },
        colors = ButtonDefaults.buttonColors(containerColor = DiaryColors.AccentBlue),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.height(48.dp)
      ) {
        Text("Задать", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }

    if (statusMessage.isNotEmpty()) {
      Spacer(modifier = Modifier.height(10.dp))
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(DiaryColors.PurpleAccentLight)
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = statusMessage,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

@Composable
fun StudentGradesViewCard(
  currentRole: UserRole,
  studentDatabase: List<InteractiveStudent>
) {
  val curSt = studentDatabase.firstOrNull() ?: InteractiveStudent("err", "Без Имени", "11-А")

  GlassyCard(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = if (currentRole == UserRole.PARENT) "Сводные оценки ребенка" else "Сводные оценки ученика",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "${curSt.name} - ${curSt.gradeClass} класс",
          fontSize = 11.sp,
          color = DiaryColors.SecondaryText
        )
      }
      Icon(
        imageVector = Icons.Rounded.PieChart,
        contentDescription = "Analytic metric summary",
        tint = DiaryColors.AccentBlue,
        modifier = Modifier.size(24.dp)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    val subjects = listOf("Геометрия", "Физика", "Литература")
    subjects.forEach { subject ->
      val gradesForThis = curSt.grades[subject] ?: emptyList()
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = subject,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          modifier = Modifier.width(90.dp)
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.weight(1f)
        ) {
          gradesForThis.forEach { singleGrade ->
            val colP = getNumericalGradeColor(singleGrade)
            val colB = getNumericalGradeBgColor(singleGrade)
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(colB),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = singleGrade,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colP
              )
            }
          }
        }

        val avg = if (gradesForThis.isNotEmpty()) {
          "%.2f".format(gradesForThis.mapNotNull { it.toIntOrNull() }.average())
        } else "0.00"
        Text(
          text = "Ср: $avg",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = DiaryColors.AccentGreen
        )
      }
    }
  }
}

@Composable
fun GradeOverviewMatrixSection(
  studentDatabase: List<InteractiveStudent>,
  subjects: List<String>
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "Оффлайн ведомость успеваемости класса",
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White,
      modifier = Modifier.padding(bottom = 6.dp)
    )

    GlassyCard(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0x22FFFFFF), RoundedCornerShape(8.dp))
          .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "Ученик", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1.2f))
        Text(text = "Геометрия", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(text = "Физика", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(text = "Литер.", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
      }

      Spacer(modifier = Modifier.height(6.dp))

      studentDatabase.forEach { student ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = student.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1.2f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          subjects.forEach { subj ->
            val list = student.grades[subj] ?: emptyList()
            val lastGrade = list.lastOrNull() ?: "—"
            
            Box(
              modifier = Modifier.weight(1f),
              contentAlignment = Alignment.Center
            ) {
              if (lastGrade != "—") {
                val cP = getNumericalGradeColor(lastGrade)
                val cB = getNumericalGradeBgColor(lastGrade)
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(cB),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = lastGrade,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = cP
                  )
                }
              } else {
                Text(text = "—", fontSize = 11.sp, color = DiaryColors.SecondaryText)
              }
            }
          }
        }
        Divider(color = Color(0x11FFFFFF), thickness = 0.5.dp)
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 2: ANALYTICS WITH CANVAS SMOOTH GRAPH RENDERING (LIQUID GLASS LINE GRAPH)
// -------------------------------------------------------------
@Composable
fun SchoolStatsTabContent() {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
  ) {
    item {
      GlassyCard(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Аналитика школы (Масштаб: 2500+ учен.)",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = DiaryColors.AccentGreen
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Автономная инфографика прогресса успеваемости и средних оценок.",
          fontSize = 11.sp,
          color = DiaryColors.SecondaryText
        )
      }
    }

    item {
      SchoolStatsIndicatorsCard()
    }

    // Interactive Custom Bezier Line Chart Drawing Panel (Satisfies "улучши график")
    item {
      AcademicBezierChartCard()
    }

    item {
      RatingStatisticsListView()
    }

    item {
      SchoolBestClassesRankingsCard()
    }
  }
}

@Composable
fun SchoolStatsIndicatorsCard() {
  GlassyCard(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "Статистика по всей школе (Рейтинг)",
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White
    )

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text("2548", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DiaryColors.AccentBlue)
        Text("Учеников всего", fontSize = 10.sp, color = DiaryColors.SecondaryText)
      }
      Box(modifier = Modifier.height(28.dp).width(1.dp).background(Color(0x22FFFFFF)))
      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text("91", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DiaryColors.AccentPurple)
        Text("Классов (1-11 ког.)", fontSize = 10.sp, color = DiaryColors.SecondaryText)
      }
      Box(modifier = Modifier.height(28.dp).width(1.dp).background(Color(0x22FFFFFF)))
      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text("28.0", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DiaryColors.AccentGreen)
        Text("Ср. размер класса", fontSize = 10.sp, color = DiaryColors.SecondaryText)
      }
    }
  }
}

// -------------------------------------------------------------
// PREMIUM SMOOTH BEZIER LINE CHART CANVAS DRAWING
// -------------------------------------------------------------
@Composable
fun AcademicBezierChartCard() {
  val graphPoints = listOf(4.15f, 4.28f, 4.35f, 4.21f, 4.45f, 4.58f, 4.62f)
  val graphLabels = listOf("Сент", "Окт", "Ноя", "Дек", "Фев", "Мар", "Май")

  GlassyCard(modifier = Modifier.fillMaxWidth()) {
    Column {
      Text(
        text = "Динамика успеваемости ГБОУ (Средний балл)",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Показатели успеваемости за текущий учебный цикл.",
        fontSize = 11.sp,
        color = DiaryColors.SecondaryText
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Bezier curve drawing inside Canvas with glassy backdrop gradient filling
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
      ) {
        val width = size.width
        val height = size.height
        val paddingX = 40f
        val paddingY = 20f

        val pointsCount = graphPoints.size
        val stepX = (width - paddingX * 2) / (pointsCount - 1)

        val minY = 4.0f
        val maxY = 4.8f

        val calculatedCoordinates = graphPoints.mapIndexed { index, value ->
          val cx = paddingX + index * stepX
          val ratio = (value - minY) / (maxY - minY)
          val cy = height - paddingY - ratio * (height - paddingY * 2)
          Offset(cx, cy)
        }

        // Draw horizontal raster references
        for (i in 0..3) {
          val yLevel = paddingY + i * (height - paddingY * 2) / 3
          drawLine(
            color = Color(0x11FFFFFF),
            start = Offset(paddingX, yLevel),
            end = Offset(width - paddingX, yLevel),
            strokeWidth = 2f
          )
        }

        // Generate smooth Bezier path
        val strokePath = Path()
        val fillPath = Path()

        if (calculatedCoordinates.isNotEmpty()) {
          val firstPoint = calculatedCoordinates[0]
          strokePath.moveTo(firstPoint.x, firstPoint.y)
          fillPath.moveTo(firstPoint.x, height - paddingY)
          fillPath.lineTo(firstPoint.x, firstPoint.y)

          for (i in 0 until calculatedCoordinates.size - 1) {
            val p0 = calculatedCoordinates[i]
            val p1 = calculatedCoordinates[i + 1]
            val controlX = (p0.x + p1.x) / 2
            
            strokePath.cubicTo(
              controlX, p0.y,
              controlX, p1.y,
              p1.x, p1.y
            )
            fillPath.cubicTo(
              controlX, p0.y,
              controlX, p1.y,
              p1.x, p1.y
            )
          }

          fillPath.lineTo(calculatedCoordinates.last().x, height - paddingY)
          fillPath.close()

          // 1. Draw area translucent fills (Green glowing gradient)
          drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
              colors = listOf(Color(0x5510B981), Color(0x0010B981))
            )
          )

          // 2. Draw curve outline
          drawPath(
            path = strokePath,
            color = DiaryColors.AccentGreen,
            style = Stroke(width = 4f)
          )

          // 3. Draw glow points and text anchors
          calculatedCoordinates.forEachIndexed { idx, point ->
            drawCircle(
              color = Color.White,
              radius = 5f,
              center = point
            )
            drawCircle(
              color = DiaryColors.AccentGreen,
              radius = 9f,
              center = point,
              style = Stroke(width = 3f)
            )
          }
        }
      }

      // X-Axis Title markings
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        graphLabels.forEach { label ->
          Text(text = label, fontSize = 10.sp, color = DiaryColors.SecondaryText, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
        }
      }
    }
  }
}

@Composable
fun RatingStatisticsListView() {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(2.dp, RoundedCornerShape(16.dp), clip = false)
      .testTag("rating_section"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
    border = BorderStroke(1.dp, Color(0x11FFFFFF))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
            contentDescription = "Analysis chart",
            tint = DiaryColors.AccentBlue,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Сводные уровни показателей классов",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
        Text(
          text = "Рейтинг 4.XX",
          fontSize = 11.sp,
          color = DiaryColors.SecondaryText,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      val items = listOf(
        SchoolClassStats("Параллель 9-х классов", 252, "4.21 / 5.0", 0.84f),
        SchoolClassStats("Параллель 10-х классов", 244, "4.38 / 5.0", 0.87f),
        SchoolClassStats("Параллель 11-х классов", 228, "4.56 / 5.0", 0.91f),
        SchoolClassStats("Параллель 8-х классов", 280, "4.15 / 5.0", 0.82f)
      )

      items.forEachIndexed { i, stat ->
        Column(modifier = Modifier.padding(vertical = 5.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = stat.className, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text(text = stat.averageScore, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DiaryColors.AccentGreen)
          }
          Spacer(modifier = Modifier.height(3.dp))
          LinearProgressIndicator(
            progress = { stat.ratingProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(5.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = if (i % 2 == 0) DiaryColors.AccentBlue else DiaryColors.AccentGreen,
            trackColor = Color(0x11FFFFFF)
          )
        }
      }
    }
  }
}

@Composable
fun SchoolBestClassesRankingsCard() {
  GlassyCard(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "Лидирующие классы в школе",
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      color = Color.White
    )
    Spacer(modifier = Modifier.height(8.dp))

    val topClasses = listOf(
      Pair("11-А класс", "Ср. балл: 4.82"),
      Pair("10-Б класс", "Ср. балл: 4.67"),
      Pair("9-А класс", "Ср. балл: 4.58"),
      Pair("11-Б класс", "Ср. балл: 4.54")
    )

    topClasses.forEachIndexed { num, data ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(22.dp)
              .clip(CircleShape)
              .background(if (num == 0) DiaryColors.PurpleAccentLight else Color(0x11FFFFFF)),
            contentAlignment = Alignment.Center
          ) {
            Text("${num + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DiaryColors.AccentPurple)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(data.first, fontSize = 12.sp, color = Color.White)
        }

        Text(data.second, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DiaryColors.SecondaryText)
      }
    }
  }
}

// -------------------------------------------------------------
// TAB 3: USER PROFILE CABINET
// -------------------------------------------------------------
@Composable
fun CabinetTabContent(
  currentRole: UserRole,
  currentUserLogin: String,
  isLoggedIn: Boolean,
  onRoleChange: (UserRole, String) -> Unit,
  onLogout: () -> Unit,
  onUpdateAnnouncement: (String, String) -> Unit
) {
  var enteredUser by remember { mutableStateOf("") }
  var enteredPass by remember { mutableStateOf("") }
  var errorAlert by remember { mutableStateOf("") }

  var pendingNoticeTitle by remember { mutableStateOf("") }
  var pendingNoticeBody by remember { mutableStateOf("") }
  var announcementLogMsg by remember { mutableStateOf("") }

  val credentialDatabaseList = listOf(
    MockUserCred("senior_admin", "1234", UserRole.SENIOR_ADMIN),
    MockUserCred("junior_admin", "5678", UserRole.JUNIOR_ADMIN),
    MockUserCred("teacher_math", "math123", UserRole.TEACHER),
    MockUserCred("parent_user", "parent99", UserRole.PARENT),
    MockUserCred("student_user", "user432", UserRole.STUDENT)
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
  ) {
    if (!isLoggedIn) {
      item {
        GlassyCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Вход в систему Дневника",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = enteredUser,
            onValueChange = { enteredUser = it },
            label = { Text("Логин", fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = enteredPass,
            onValueChange = { enteredPass = it },
            label = { Text("Пароль", fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )

          if (errorAlert.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(errorAlert, color = DiaryColors.AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              val matched = credentialDatabaseList.find { it.user == enteredUser && it.pass == enteredPass }
              if (matched != null) {
                onRoleChange(matched.role, matched.user)
                enteredUser = ""
                enteredPass = ""
                errorAlert = ""
              } else {
                errorAlert = "Неверный логин или пароль!"
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DiaryColors.AccentBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Авторизоваться", fontWeight = FontWeight.Bold)
          }
        }
      }
    } else {
      item {
        GlassyCard(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Личный Кабинет",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Вы вошли как: $currentUserLogin",
            fontSize = 12.sp,
            color = DiaryColors.SecondaryText
          )
          Spacer(modifier = Modifier.height(4.dp))

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(currentRole.badgeColor)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(currentRole.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = DiaryColors.AccentRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Выйти из аккаунта", fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }

      if (currentRole == UserRole.SENIOR_ADMIN || currentRole == UserRole.JUNIOR_ADMIN) {
        item {
          GlassyCard(modifier = Modifier.fillMaxWidth()) {
            Text(
              text = "Управление объявлениями (Важное)",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = pendingNoticeTitle,
              onValueChange = { pendingNoticeTitle = it },
              label = { Text("Заголовок новости", fontSize = 12.sp) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = pendingNoticeBody,
              onValueChange = { pendingNoticeBody = it },
              label = { Text("Текст школьного объявления...", fontSize = 12.sp) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
              onClick = {
                if (pendingNoticeTitle.isNotEmpty() && pendingNoticeBody.isNotEmpty()) {
                  onUpdateAnnouncement(pendingNoticeTitle, pendingNoticeBody)
                  announcementLogMsg = "Объявление успешно зафиксировано!"
                  pendingNoticeTitle = ""
                  pendingNoticeBody = ""
                } else {
                  announcementLogMsg = "Заполните текстовые поля объявления!"
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = DiaryColors.AccentPurple),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Обновить Школьное Объявление", fontWeight = FontWeight.Bold)
            }

            if (announcementLogMsg.isNotEmpty()) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(announcementLogMsg, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          }
        }
      }
    }

    item {
      GlassyCard(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Быстрое переключение ролей",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Консоль переходов для быстрого тестирования прав учителей, администраторов и учеников:",
          fontSize = 11.sp,
          color = DiaryColors.SecondaryText
        )
        Spacer(modifier = Modifier.height(10.dp))

        credentialDatabaseList.forEach { cred ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onRoleChange(cred.role, cred.user)
              }
              .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = cred.role.displayName,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(cred.role.badgeColor)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(cred.user, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DiaryColors.WarningAccentLight),
        border = BorderStroke(1.dp, Color(0x22F59E0B))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.CloudOff, contentDescription = "Offline server mode badge", tint = DiaryColors.WarningPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Справочник оффлайн-доступов к БД",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Для локальной проверки учетных записей воспользуйтесь реквизитами доступа:",
            fontSize = 11.sp,
            color = DiaryColors.SecondaryText
          )

          Spacer(modifier = Modifier.height(10.dp))

          credentialDatabaseList.forEach { valCred ->
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
              Text(
                text = "• ${valCred.role.displayName}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Логин: ${valCred.user}  |  Пароль: ${valCred.pass}",
                fontSize = 11.sp,
                color = DiaryColors.SecondaryText
              )
            }
          }
        }
      }
    }
  }
}

data class MockUserCred(
  val user: String,
  val pass: String,
  val role: UserRole
)

// -------------------------------------------------------------
// INTERACTIVE DIALOG COMPONENTS
// -------------------------------------------------------------
@Composable
fun BeautifulGradeDetailDialog(item: GradeItemCard, onDismiss: () -> Unit) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
      border = BorderStroke(1.dp, Color(0x22FFFFFF)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Паспорт Оценки (Оффлайн)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close Window", tint = DiaryColors.SecondaryText)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val colP = getNumericalGradeColor(item.gradeValue)
        val colB = getNumericalGradeBgColor(item.gradeValue)

        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(colB),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = item.gradeValue,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colP
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = item.subjectName,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = item.testType,
          fontSize = 13.sp,
          color = DiaryColors.SecondaryText
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x22000000), RoundedCornerShape(12.dp))
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Дата урока", fontSize = 10.sp, color = DiaryColors.SecondaryText)
            Text(item.examDate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Коэффициент", fontSize = 10.sp, color = DiaryColors.SecondaryText)
            Text("1.5 x", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Проверил", fontSize = 10.sp, color = DiaryColors.SecondaryText)
            Text("Система РФ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = DiaryColors.AccentBlue),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Понятно", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun QrPromotionInfoDialog(onDismiss: () -> Unit) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
      border = BorderStroke(1.dp, Color(0x22FFFFFF)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Мгновенный вход по QR",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close Promo Window", tint = DiaryColors.SecondaryText)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
          modifier = Modifier
            .size(160.dp)
            .background(Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            .border(2.dp, DiaryColors.AccentPurple, RoundedCornerShape(16.dp))
            .padding(14.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              QrDesignCorner()
              QrDesignCorner()
            }
            Box(
              modifier = Modifier
                .size(40.dp)
                .background(Color(0x338B5CF6), RoundedCornerShape(8.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Rounded.QrCodeScanner,
                contentDescription = "Simulated symbol center",
                tint = DiaryColors.AccentPurple,
                modifier = Modifier.size(24.dp)
              )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              QrDesignCorner()
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .background(DiaryColors.AccentPurple, RoundedCornerShape(4.dp))
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Инструкция входа",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "Нажмите 'Оформить доступы' на домашнем терминале школы, отсканируйте код камерой для мгновенного входа.",
          fontSize = 11.sp,
          color = DiaryColors.SecondaryText,
          textAlign = TextAlign.Center,
          lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = DiaryColors.AccentPurple),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Понятно", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun QrDesignCorner() {
  Box(
    modifier = Modifier
      .size(24.dp)
      .border(2.5.dp, Color.White, RoundedCornerShape(4.dp))
      .padding(3.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.White, RoundedCornerShape(2.2.dp))
    )
  }
}

@Composable
fun FullScreenLoginView(
  onRoleChange: (UserRole, String) -> Unit,
  credentialDatabaseList: List<MockUserCred>
) {
  var enteredUser by remember { mutableStateOf("") }
  var enteredPass by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var errorAlert by remember { mutableStateOf("") }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(16.dp, RoundedCornerShape(24.dp), clip = false)
        .testTag("login_card"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = Color(0xCF0F172A) // Frosted Dark Slate
      ),
      border = BorderStroke(1.5.dp, Color(0x33FFFFFF)) // Sparkling edge polish
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Glowing key badge
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color(0x223B82F6))
            .border(1.5.dp, DiaryColors.AccentBlue, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Rounded.VpnKey,
            contentDescription = "🔑 Key Icon",
            tint = DiaryColors.AccentBlue,
            modifier = Modifier.size(36.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Вход в Дневник.Ру",
          fontSize = 22.sp,
          fontWeight = FontWeight.Black,
          color = Color.White,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Авторизуйтесь для просмотра дневника, сеток успеваемости и школьного рейтинга",
          fontSize = 12.sp,
          color = DiaryColors.SecondaryText,
          textAlign = TextAlign.Center,
          lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Username Field
        OutlinedTextField(
          value = enteredUser,
          onValueChange = {
            enteredUser = it
            errorAlert = ""
          },
          label = { Text("Логин", fontSize = 12.sp) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Rounded.Person,
              contentDescription = "User icon",
              tint = DiaryColors.AccentBlue
            )
          },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = DiaryColors.AccentBlue,
            unfocusedBorderColor = Color(0x33FFFFFF),
            focusedLabelColor = DiaryColors.AccentBlue,
            unfocusedLabelColor = DiaryColors.SecondaryText
          ),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password Field
        OutlinedTextField(
          value = enteredPass,
          onValueChange = {
            enteredPass = it
            errorAlert = ""
          },
          label = { Text("Пароль", fontSize = 12.sp) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Rounded.Lock,
              contentDescription = "Lock icon",
              tint = DiaryColors.AccentPurple
            )
          },
          trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
              Icon(
                imageVector = if (isPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                contentDescription = "Show/hide password toggle",
                tint = DiaryColors.SecondaryText
              )
            }
          },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = DiaryColors.AccentPurple,
            unfocusedBorderColor = Color(0x33FFFFFF),
            focusedLabelColor = DiaryColors.AccentPurple,
            unfocusedLabelColor = DiaryColors.SecondaryText
          ),
          visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )

        if (errorAlert.isNotEmpty()) {
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0x22EF4444))
              .padding(8.dp)
          ) {
            Icon(Icons.Rounded.ErrorOutline, contentDescription = "Error notification", tint = DiaryColors.AccentRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(errorAlert, color = DiaryColors.AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            val trimmedUser = enteredUser.trim()
            val trimmedPass = enteredPass.trim()
            val matched = credentialDatabaseList.find { it.user == trimmedUser && it.pass == trimmedPass }
            if (matched != null) {
              onRoleChange(matched.role, matched.user)
            } else {
              errorAlert = "Неверный логин или пароль! Попробуйте предложенные демо-аккаунты ниже."
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = DiaryColors.AccentBlue),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Войти в систему", fontWeight = FontWeight.Black, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Rounded.Login, contentDescription = "Login trigger icon", modifier = Modifier.size(16.dp))
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Divider(color = Color(0x18FFFFFF), thickness = 1.dp)

        Spacer(modifier = Modifier.height(16.dp))

        // Demo login section title
        Text(
          text = "⚡ БЫСТРЫЙ ДЕМО-ВХОД БЕЗ ВВОДА ДАННЫХ",
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          color = DiaryColors.AccentGreen,
          letterSpacing = 0.5.sp,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Нажмите на нужную роль для мгновенного входа и проверки прав:",
          fontSize = 10.sp,
          color = DiaryColors.SecondaryText,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          credentialDatabaseList.forEach { cred ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x10FFFFFF))
                .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                .clickable {
                  onRoleChange(cred.role, cred.user)
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(cred.role.titleColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = cred.role.displayName,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(cred.role.badgeColor.copy(alpha = 0.4f))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "Войти как: ${cred.user} 🔑",
                  fontSize = 10.sp,
                  color = cred.role.titleColor,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}
