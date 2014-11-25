CRCCheck On

!define MUI_PRODUCT "ACT Client"

;--------------------------------
;Plugins dir
!addplugindir nsis

;--------------------------------
;Include Modern UI
!include "MUI2.nsh"

;---------------------------------
;General

Name "${MUI_PRODUCT}"
BrandingText "${MUI_PRODUCT} v${VERSION}"

OutFile "ACT-${PLATFORM}-${VERSION}.exe"

ShowInstDetails "nevershow"
ShowUninstDetails "nevershow"

!define ACT_REGKEY "Software\ACT\Client"
!define INSTALL_DIR_REGNAME "InstallDir"
!define IS_INSTALLED_REGNAME "IsInstalled"

!ifdef ON64BITS
  InstallDir "$PROGRAMFILES64\ACT\Client"
!else
  InstallDir "$PROGRAMFILES\ACT\Client"
!endif

!define START_MENU_GROUP "ACT Client"

;Get installation folder from registry if available
InstallDirRegKey HKLM "${ACT_REGKEY}" "${INSTALL_DIR_REGNAME}"

;--------------------------------
;Exe details

VIProductVersion                 "${VIVERSION}"
VIAddVersionKey ProductName      "ACT Client"
VIAddVersionKey Comments         "Assisted Contact Tracing"
VIAddVersionKey CompanyName      "ACT"
VIAddVersionKey LegalCopyright   "ACT"
VIAddVersionKey FileDescription  "Assisted Contact Tracing client"
VIAddVersionKey FileVersion      "${VERSION}"
VIAddVersionKey ProductVersion   "${VERSION}"
VIAddVersionKey InternalName     "ACTClient"
VIAddVersionKey LegalTrademarks  "ACT"

;--------------------------------
;Interface Settings

!define MUI_ABORTWARNING

;--------------------------------
;Pages

!insertmacro MUI_PAGE_LICENSE "license.txt"
!insertmacro MUI_PAGE_INSTFILES

SpaceTexts none
!define MUI_COMPONENTSPAGE_NODESC
!insertmacro MUI_UNPAGE_COMPONENTS
!insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------
;Languages

!insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "Install" Install
  SetOutPath "$INSTDIR"
  File "license.txt"

  CreateDirectory "C:\ACT\data"

  ;Add files
  SetOutPath "$INSTDIR\bin"
  File "bin\RCEDIT.exe"
  File /oname=ACT-client.exe "bin\WinRun4J.exe"
  File "bin\act-client.ini"
  SetOutPath "$INSTDIR\lib"
  File "lib\*.jar"

  SetOutPath "$INSTDIR\json"
  File "json\locations-packed.json"

  SetOutPath "$INSTDIR\jre"
  File /r "jre1.7.0_71\*.*"

; TODO: extract to a $INSTDIR\rsync subdir
  SetOutPath "$INSTDIR"
  File /r "cwRsync\*.*"

  WriteUninstaller "$INSTDIR\Uninstall.exe"
SectionEnd

Section "Build Launcher"
  DetailPrint "Building launcher..."
  SetOutPath "$INSTDIR\bin"
  ExecDos::exec 'RCEDIT.exe /N ACT-client.exe act-client.ini' "" "$TEMP/act-installer.log"
  Delete "act-client.ini"
  Delete "rcedit.exe"
SectionEnd

Section "Create Shortcuts"
  SetOutPath "$INSTDIR"

  ;create desktop shortcut
  CreateShortCut "$DESKTOP\${MUI_PRODUCT}.lnk" "$INSTDIR\bin\ACT-client.exe" ""

  ;create start-menu items
  CreateDirectory "$SMPROGRAMS\${START_MENU_GROUP}"
  CreateShortCut "$SMPROGRAMS\${START_MENU_GROUP}\Uninstall.lnk" "$INSTDIR\Uninstall.exe" "" "$INSTDIR\Uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\${START_MENU_GROUP}\${MUI_PRODUCT}.lnk" "$INSTDIR\bin\ACT-client.exe" "" "$INSTDIR\bin\ACT-client.exe" 0

;write uninstall information to the registry
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${MUI_PRODUCT}" "DisplayName" "${MUI_PRODUCT}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${MUI_PRODUCT}" "UninstallString" "$INSTDIR\Uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${MUI_PRODUCT}" "Publisher" "ACT"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${MUI_PRODUCT}" "DisplayVersion" "${VERSION}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${MUI_PRODUCT}" "DisplayIcon" "$INSTDIR\Uninstall.exe"

  ;Store a flag when installed
  WriteRegDWORD HKLM "${ACT_REGKEY}" "${IS_INSTALLED_REGNAME}" 0x1

  ;Store installation folder
  WriteRegStr HKLM "${ACT_REGKEY}" "${INSTALL_DIR_REGNAME}" $INSTDIR

SectionEnd

;--------------------------------
;Uninstaller Sections

Section "un.Remove ACT Client"
  SectionIn RO

;Delete Files
  RMDir /r "$INSTDIR\bin\*.*"
  RMDir /r "$INSTDIR\lib\*.*"
  RMDir /r "$INSTDIR\jre\*.*"
  Delete "$INSTDIR\license.txt"

;Delete Start Menu Shortcuts
  Delete "$DESKTOP\${MUI_PRODUCT}.lnk"
  Delete "$SMPROGRAMS\${START_MENU_GROUP}\*.*"
  RmDir  "$SMPROGRAMS\${START_MENU_GROUP}"

;Delete Uninstaller And Unistall Registry Entries
  DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\${MUI_PRODUCT}"
  DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\${MUI_PRODUCT}"

  Delete "$INSTDIR\Uninstall.exe"

  DeleteRegValue HKLM "${ACT_REGKEY}" "${IS_INSTALLED_REGNAME}"

  DeleteRegKey /ifempty HKLM "${ACT_REGKEY}"

  RMDir /r "$INSTDIR"
SectionEnd