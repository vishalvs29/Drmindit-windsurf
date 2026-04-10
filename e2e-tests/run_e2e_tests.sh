#!/bin/bash

# DrMindit End-to-End Test Execution Script
# Runs comprehensive E2E tests for the DrMindit application

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_APP_DIR="$PROJECT_DIR/androidApp"
TEST_RESULTS_DIR="$PROJECT_DIR/e2e-test-results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_DIR="$TEST_RESULTS_DIR/report_$TIMESTAMP"

# Create directories
mkdir -p "$REPORT_DIR"
mkdir -p "$REPORT_DIR/screenshots"
mkdir -p "$REPORT_DIR/logs"
mkdir -p "$REPORT_DIR/videos"

echo -e "${BLUE}=== DrMindit E2E Test Suite ===${NC}"
echo -e "${BLUE}Starting comprehensive end-to-end testing...${NC}"
echo ""

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check if Android SDK is available
    if ! command -v adb &> /dev/null; then
        print_error "Android SDK (adb) not found. Please install Android SDK."
        exit 1
    fi
    
    # Check if gradle is available
    if ! command -v gradle &> /dev/null && [ ! -f "$ANDROID_APP_DIR/gradlew" ]; then
        print_error "Gradle not found. Please install Gradle or use gradlew."
        exit 1
    fi
    
    # Check if device/emulator is available
    if ! adb devices | grep -q "device$\|emulator"; then
        print_warning "No Android device/emulator found. Starting emulator..."
        
        # Try to start an emulator (adjust emulator name as needed)
        if command -v emulator &> /dev/null; then
            emulator -avd test_emulator -no-audio -no-window &
            EMULATOR_PID=$!
            
            # Wait for emulator to boot
            print_status "Waiting for emulator to boot..."
            for i in {1..60}; do
                if adb shell getprop sys.boot_completed | grep -q "1"; then
                    print_status "Emulator is ready"
                    break
                fi
                sleep 2
            done
        else
            print_error "No emulator available. Please start an Android device or emulator."
            exit 1
        fi
    fi
    
    print_status "Prerequisites check completed"
}

# Function to setup test environment
setup_test_environment() {
    print_status "Setting up test environment..."
    
    # Clean previous builds
    cd "$ANDROID_APP_DIR"
    if [ -f "gradlew" ]; then
        ./gradlew clean
    fi
    
    # Build debug APK
    print_status "Building debug APK..."
    if [ -f "gradlew" ]; then
        ./gradlew assembleDebug
    else
        gradle assembleDebug
    fi
    
    # Install APK
    print_status "Installing APK..."
    adb install -r "build/outputs/apk/debug/androidApp-debug.apk" || {
        print_warning "APK installation failed, trying uninstall first..."
        adb uninstall com.drmindit.android || true
        adb install -r "build/outputs/apk/debug/androidApp-debug.apk"
    }
    
    # Clear app data for clean test
    print_status "Clearing app data..."
    adb shell pm clear com.drmindit.android || true
    
    print_status "Test environment setup completed"
}

# Function to setup Supabase test data
setup_supabase_data() {
    print_status "Setting up Supabase test data..."
    
    # Check if Supabase CLI is available
    if command -v supabase &> /dev/null; then
        # Start local Supabase if not running
        if ! supabase status &> /dev/null; then
            print_status "Starting local Supabase..."
            supabase start
        fi
        
        # Load test schema and data
        print_status "Loading test schema..."
        supabase db reset --confirm
        
        print_status "Loading test data..."
        psql "$(supabase db url -k database_url)" -f "$PROJECT_DIR/supabase/production_schema.sql"
        psql "$(supabase db url -k database_url)" -f "$PROJECT_DIR/supabase/production_sample_data.sql"
        psql "$(supabase db url -k database_url)" -f "$PROJECT_DIR/supabase/storage_setup_fixed.sql"
        
    else
        print_warning "Supabase CLI not found. Using remote Supabase..."
        # You would need to manually set up test data in remote Supabase
        print_warning "Please ensure test data is loaded in your Supabase project"
    fi
    
    print_status "Supabase setup completed"
}

# Function to run E2E tests
run_e2e_tests() {
    print_status "Running E2E tests..."
    
    cd "$ANDROID_APP_DIR"
    
    # Start screen recording (if available)
    if command -v adb &> /dev/null; then
        print_status "Starting screen recording..."
        adb shell screenrecord "/sdcard/e2e_test_$TIMESTAMP.mp4" &
        SCREENRECORD_PID=$!
        sleep 2  # Give it time to start
    fi
    
    # Run the E2E tests
    print_status "Executing E2E test suite..."
    TEST_START_TIME=$(date +%s)
    
    if [ -f "gradlew" ]; then
        ./gradlew connectedAndroidTest \
            -Pandroid.testInstrumentationRunnerArguments.class=com.drmindit.android.e2e.DrMinditE2ETestSuite \
            --info \
            --stacktrace \
            2>&1 | tee "$REPORT_DIR/logs/e2e_test_log_$TIMESTAMP.txt"
    else
        gradle connectedAndroidTest \
            -Pandroid.testInstrumentationRunnerArguments.class=com.drmindit.android.e2e.DrMinditE2ETestSuite \
            --info \
            --stacktrace \
            2>&1 | tee "$REPORT_DIR/logs/e2e_test_log_$TIMESTAMP.txt"
    fi
    
    TEST_END_TIME=$(date +%s)
    TEST_DURATION=$((TEST_END_TIME - TEST_START_TIME))
    
    # Stop screen recording
    if [ ! -z "$SCREENRECORD_PID" ]; then
        kill $SCREENRECORD_PID 2>/dev/null || true
        sleep 2
        adb pull "/sdcard/e2e_test_$TIMESTAMP.mp4" "$REPORT_DIR/videos/"
        adb shell rm "/sdcard/e2e_test_$TIMESTAMP.mp4"
    fi
    
    # Capture screenshots
    print_status "Capturing screenshots..."
    adb shell screencap -p > "$REPORT_DIR/screenshots/final_screen_$TIMESTAMP.png"
    
    print_status "E2E tests completed in ${TEST_DURATION}s"
}

# Function to run performance tests
run_performance_tests() {
    print_status "Running performance tests..."
    
    cd "$ANDROID_APP_DIR"
    
    if [ -f "gradlew" ]; then
        ./gradlew connectedAndroidTest \
            -Pandroid.testInstrumentationRunnerArguments.class=com.drmindit.android.e2e.E2EPerformanceTestSuite \
            --info \
            2>&1 | tee "$REPORT_DIR/logs/performance_test_log_$TIMESTAMP.txt"
    else
        gradle connectedAndroidTest \
            -Pandroid.testInstrumentationRunnerArguments.class=com.drmindit.android.e2e.E2EPerformanceTestSuite \
            --info \
            2>&1 | tee "$REPORT_DIR/logs/performance_test_log_$TIMESTAMP.txt"
    fi
    
    print_status "Performance tests completed"
}

# Function to run security tests
run_security_tests() {
    print_status "Running security tests..."
    
    cd "$ANDROID_APP_DIR"
    
    if [ -f "gradlew" ]; then
        ./gradlew connectedAndroidTest \
            -Pandroid.testInstrumentationRunnerArguments.class=com.drmindit.android.e2e.E2ESecurityTestSuite \
            --info \
            2>&1 | tee "$REPORT_DIR/logs/security_test_log_$TIMESTAMP.txt"
    else
        gradle connectedAndroidTest \
            -Pandroid.testInstrumentationRunnerArguments.class=com.drmindit.android.e2e.E2ESecurityTestSuite \
            --info \
            2>&1 | tee "$REPORT_DIR/logs/security_test_log_$TIMESTAMP.txt"
    fi
    
    print_status "Security tests completed"
}

# Function to analyze test results
analyze_results() {
    print_status "Analyzing test results..."
    
    cd "$ANDROID_APP_DIR"
    
    # Generate test report
    cat > "$REPORT_DIR/test_report_$TIMESTAMP.md" << EOF
# DrMindit E2E Test Report

## Test Execution Summary
- **Date**: $(date)
- **Duration**: ${TEST_DURATION}s
- **Device**: $(adb devices | grep "device$\|emulator" | head -1 | awk '{print $1}')
- **Android Version**: $(adb shell getprop ro.build.version.release | tr -d '\r')

## Test Results

### E2E Test Suite
$(grep -E "(PASSED|FAILED|SKIPPED)" "$REPORT_DIR/logs/e2e_test_log_$TIMESTAMP.txt" | tail -20)

### Performance Test Suite
$(grep -E "(PASSED|FAILED|SKIPPED)" "$REPORT_DIR/logs/performance_test_log_$TIMESTAMP.txt" | tail -10)

### Security Test Suite
$(grep -E "(PASSED|FAILED|SKIPPED)" "$REPORT_DIR/logs/security_test_log_$TIMESTAMP.txt" | tail -10)

## Test Coverage
- Authentication flows
- Session discovery and playback
- Progress tracking
- Profile management
- Analytics
- Error handling
- Performance benchmarks
- Security validation

## Artifacts
- [Test Logs]($REPORT_DIR/logs/)
- [Screenshots]($REPORT_DIR/screenshots/)
- [Video Recording]($REPORT_DIR/videos/)
- [Test Report]($REPORT_DIR/test_report_$TIMESTAMP.md)

## Recommendations
$(if [ -f "$REPORT_DIR/logs/e2e_test_log_$TIMESTAMP.txt" ]; then
    if grep -q "FAILED" "$REPORT_DIR/logs/e2e_test_log_$TIMESTAMP.txt"; then
        echo "- Review failed tests and fix issues"
        echo "- Run tests again after fixes"
    else
        echo "- All tests passed! Ready for production"
    fi
else
    echo "- Test logs not found"
fi)
EOF
    
    # Count test results
    local total_tests=0
    local passed_tests=0
    local failed_tests=0
    
    if [ -f "$REPORT_DIR/logs/e2e_test_log_$TIMESTAMP.txt" ]; then
        total_tests=$(grep -c "test.*:" "$REPORT_DIR/logs/e2e_test_log_$TIMESTAMP.txt" || echo "0")
        passed_tests=$(grep -c "PASSED" "$REPORT_DIR/logs/e2e_test_log_$TIMESTAMP.txt" || echo "0")
        failed_tests=$(grep -c "FAILED" "$REPORT_DIR/logs/e2e_test_log_$TIMESTAMP.txt" || echo "0")
    fi
    
    echo -e "${GREEN}=== Test Results Summary ===${NC}"
    echo -e "Total Tests: ${BLUE}$total_tests${NC}"
    echo -e "Passed: ${GREEN}$passed_tests${NC}"
    echo -e "Failed: ${RED}$failed_tests${NC}"
    echo -e "Success Rate: ${GREEN}$(( passed_tests * 100 / total_tests ))%${NC}" 2>/dev/null || echo "N/A"
    echo ""
    
    # Show report location
    echo -e "${BLUE}Test report generated: ${GREEN}$REPORT_DIR/test_report_$TIMESTAMP.md${NC}"
    echo -e "${BLUE}All artifacts saved to: ${GREEN}$REPORT_DIR${NC}"
}

# Function to cleanup
cleanup() {
    print_status "Cleaning up..."
    
    # Stop emulator if we started it
    if [ ! -z "$EMULATOR_PID" ]; then
        kill $EMULATOR_PID 2>/dev/null || true
    fi
    
    # Stop screen recording if still running
    if [ ! -z "$SCREENRECORD_PID" ]; then
        kill $SCREENRECORD_PID 2>/dev/null || true
    fi
    
    # Stop local Supabase if we started it
    if command -v supabase &> /dev/null; then
        supabase stop &> /dev/null || true
    fi
    
    print_status "Cleanup completed"
}

# Main execution
main() {
    trap cleanup EXIT
    
    print_status "Starting DrMindit E2E Test Suite..."
    echo ""
    
    check_prerequisites
    setup_test_environment
    setup_supabase_data
    run_e2e_tests
    run_performance_tests
    run_security_tests
    analyze_results
    
    echo ""
    echo -e "${GREEN}=== E2E Test Suite Completed ===${NC}"
    echo -e "${GREEN}All tests executed successfully!${NC}"
    echo ""
    echo -e "${BLUE}Next steps:${NC}"
    echo "1. Review the test report: $REPORT_DIR/test_report_$TIMESTAMP.md"
    echo "2. Check screenshots and videos in: $REPORT_DIR"
    echo "3. Fix any failed tests if needed"
    echo "4. Run tests again before production deployment"
}

# Handle command line arguments
case "${1:-}" in
    --help|-h)
        echo "Usage: $0 [options]"
        echo ""
        echo "Options:"
        echo "  --help, -h     Show this help message"
        echo "  --clean        Clean test results only"
        echo "  --setup        Setup test environment only"
        echo "  --e2e          Run E2E tests only"
        echo "  --performance  Run performance tests only"
        echo "  --security     Run security tests only"
        echo ""
        exit 0
        ;;
    --clean)
        rm -rf "$TEST_RESULTS_DIR"
        print_status "Test results cleaned"
        exit 0
        ;;
    --setup)
        check_prerequisites
        setup_test_environment
        setup_supabase_data
        exit 0
        ;;
    --e2e)
        check_prerequisites
        setup_test_environment
        setup_supabase_data
        run_e2e_tests
        analyze_results
        exit 0
        ;;
    --performance)
        check_prerequisites
        setup_test_environment
        run_performance_tests
        exit 0
        ;;
    --security)
        check_prerequisites
        setup_test_environment
        run_security_tests
        exit 0
        ;;
    *)
        main
        ;;
esac
