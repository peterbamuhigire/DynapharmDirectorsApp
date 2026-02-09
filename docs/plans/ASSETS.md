# Project Assets

**Last Updated**: 2026-02-09

---

## Logo

**Dynapharm Logo Location**: `dist/img/icons/DynaLogo.png`

**Usage**:
- Login screen (centered, above login form)
- Splash screen
- App icon (may need resizing/adaptation)
- About screen
- Any branding locations throughout the app

**Implementation Notes**:
- Use Coil for loading: `AsyncImage(model = "file:///android_asset/dist/img/icons/DynaLogo.png", ...)`
- Ensure proper scaling for different screen densities
- Maintain aspect ratio
- Consider creating multiple drawable densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi) from source

---

## App Icons

**To Do**: Generate adaptive icon set from DynaLogo.png

Required icon assets:
- `mipmap-mdpi/ic_launcher.png` (48x48)
- `mipmap-hdpi/ic_launcher.png` (72x72)
- `mipmap-xhdpi/ic_launcher.png` (96x96)
- `mipmap-xxhdpi/ic_launcher.png` (144x144)
- `mipmap-xxxhdpi/ic_launcher.png` (192x192)
- `mipmap-anydpi-v26/ic_launcher.xml` (adaptive icon XML)

Use Android Studio's Image Asset Studio:
1. Right-click `res` folder → New → Image Asset
2. Select "Launcher Icons (Adaptive and Legacy)"
3. Path: `dist/img/icons/DynaLogo.png`
4. Adjust padding/scaling
5. Generate all densities

---

## Screenshots (For Play Store)

Required for Play Store submission (Section 8 of Phase 1):

1. **Login Screen** - Show Dynapharm logo, language selector
2. **Dashboard** - 5 KPI cards with data
3. **Franchise Switcher** - Multi-franchise list
4. **Sales Report** - Chart + table view
5. **P&L Report** - Hierarchical financial data
6. **Approval Queue** - Pending items list
7. **Approval Detail** - Expense detail with approve/reject
8. **Profile Screen** - Owner info and settings

**Specifications**:
- Format: PNG or JPEG
- Dimensions: 16:9 or 9:16 ratio
- Min width: 320px
- Max width: 3840px
- Quantity: 4-8 screenshots per device type

**Device Types**:
- Phone (1080x1920 recommended)
- 7-inch tablet (1200x1920 recommended)
- 10-inch tablet (1600x2560 recommended)

---

## Feature Graphic (Play Store)

**Specifications**:
- Dimensions: 1024 x 500 px
- Format: PNG or JPEG
- Content: Dynapharm branding + "Owner Hub" text + key feature icons
- Design tool: Canva, Figma, or Adobe Illustrator

---

## Additional Assets Needed

| Asset | Purpose | Status |
|-------|---------|--------|
| Splash screen | App launch screen | To Do |
| Error illustrations | Empty states, errors | Optional |
| Chart placeholder | Loading state for charts | Optional |
| Onboarding images | First-run tutorial (v1.1) | Future |
| Notification icon | Push notifications (Phase 3) | Future |

---

## Asset Organization

```
app/src/main/
├── res/
│   ├── drawable/           # Vector drawables, XMLs
│   │   └── ic_*.xml        # Icon assets
│   ├── mipmap-*/           # App launcher icons (all densities)
│   │   └── ic_launcher.*
│   └── values/
│       └── strings.xml     # App name, content descriptions
└── assets/
    └── dist/
        └── img/
            └── icons/
                └── DynaLogo.png  # Source logo file
```

---

## Color Palette

Extract from Dynapharm brand guidelines:

| Color | Hex | Usage |
|-------|-----|-------|
| Primary Green | `#2E7D32` (suggested) | Buttons, headers, active states |
| Secondary | TBD | Accents, highlights |
| Background Light | `#FFFFFF` | Light theme background |
| Background Dark | `#121212` | Dark theme background |
| Error | `#B00020` | Error states, validation |
| Success | `#388E3C` | Success messages, positive indicators |

**To Do**: Confirm exact brand colors from Dynapharm marketing team.

---

## Typography

**Font Family**: Roboto (Material Design default) or custom brand font

If using custom font:
1. Obtain `.ttf` or `.otf` font files
2. Place in `app/src/main/res/font/`
3. Define in `presentation/theme/Type.kt`

---

## Icon Set

Material Icons (already included in Compose):
- `Icons.Default.Home` - Dashboard tab
- `Icons.Default.BarChart` - Reports tab
- `Icons.Default.CheckCircle` - Approvals tab
- `Icons.Default.Store` - Franchises tab
- `Icons.Default.MoreHoriz` - More tab
- `Icons.Default.Person` - Profile
- `Icons.Default.Settings` - Settings
- `Icons.Default.Logout` - Logout
- `Icons.Default.Refresh` - Pull-to-refresh
- `Icons.Default.CloudOff` - Offline indicator

Additional icons as needed from Material Symbols.

---

## References

- Play Store Asset Requirements: [docs/android-app-owner/07_RELEASE_PLAN.md](../android-app-owner/07_RELEASE_PLAN.md#13-required-assets)
- Theme Setup: [docs/android-app-owner/phase-1/08-theme-ui-components.md](../android-app-owner/phase-1/08-theme-ui-components.md)
- Android Image Asset Studio: https://developer.android.com/studio/write/create-app-icons

---

**Next Steps**:
1. ✅ Confirm logo location: `dist/img/icons/DynaLogo.png`
2. ⏳ Verify logo file exists and is high resolution
3. ⏳ Generate adaptive icon set from logo
4. ⏳ Confirm brand color palette
5. ⏳ Create Play Store screenshots after Phase 1 complete
