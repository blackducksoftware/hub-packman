package com.synopsys.integration.detectable.detectables.clang.packagemanager.resolver;

import java.io.File;
import java.util.List;

import com.synopsys.integration.detectable.detectable.executable.ExecutableRunner;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunnerException;
import com.synopsys.integration.detectable.detectables.clang.packagemanager.ClangPackageManagerInfo;
import com.synopsys.integration.detectable.detectables.clang.packagemanager.PackageDetails;

public interface ClangPackageManagerResolver {
    List<PackageDetails> resolvePackages(ClangPackageManagerInfo currentPackageManager, ExecutableRunner executableRunner, File workingDirectory, String queryPackageOutput) throws ExecutableRunnerException;
}
