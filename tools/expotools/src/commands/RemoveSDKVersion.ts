import chalk from 'chalk';
import semver from 'semver';
import inquirer from 'inquirer';
import { Command } from '@expo/commander';

import * as IosVersioning from '../versioning/ios';
import * as AndroidVersioning from '../versioning/android';
import { getExpoRepositoryRootDir } from '../Directories';
import { Platform, getSDKVersionsAsync, getOldestSDKVersionAsync } from '../ProjectVersions';

type ActionOptions = {
  platform: Platform;
  sdkVersion?: string;
}

const SUPPORTED_PLATFORMS: Platform[] = ['ios', 'android'];
const EXPO_DIR = getExpoRepositoryRootDir();

async function getOldestOrAskForSDKVersionAsync(platform: Platform): Promise<string | undefined> {
  const sdkVersions = await getSDKVersionsAsync(platform);
  const defaultSdkVersion = await getOldestSDKVersionAsync(platform);

  if (defaultSdkVersion && process.env.CI) {
    console.log(`${chalk.red('`--sdkVersion`')} not provided - defaulting to ${chalk.cyan(defaultSdkVersion)}`);
    return defaultSdkVersion;
  }

  const { sdkVersion } = await inquirer.prompt<{ sdkVersion: string }>([
    {
      type: 'list',
      name: 'sdkVersion',
      message: 'What is the SDK version that you want to remove?',
      default: defaultSdkVersion,
      choices: sdkVersions,
      validate(value) {
        if (!semver.valid(value)) {
          return `Invalid version: ${chalk.cyan(value)}`;
        }
        return true;
      },
    }
  ]);
  return sdkVersion;
}

async function askForPlatformAsync(): Promise<Platform> {
  if (process.env.CI) {
    throw new Error(`Run with \`--platform <${SUPPORTED_PLATFORMS.join(' | ')}>\`.`);
  }

  const { platform } = await inquirer.prompt<{ platform: Platform }>([
    {
      type: 'list',
      name: 'platform',
      message: 'Choose a platform from which you want to remove SDK version:',
      default: SUPPORTED_PLATFORMS[0],
      choices: SUPPORTED_PLATFORMS,
    },
  ]);

  return platform;
}

async function action(options: ActionOptions) {
  const platform = options.platform || await askForPlatformAsync();
  const sdkVersion = options.sdkVersion || await getOldestOrAskForSDKVersionAsync(options.platform);

  if (!sdkVersion) {
    throw new Error('Oldest SDK version not found. Try to run with `--sdkVersion <SDK version>`.');
  }

  switch (platform) {
    case 'ios':
      return IosVersioning.removeVersionAsync(sdkVersion, EXPO_DIR);
    case 'android':
      return AndroidVersioning.removeVersionAsync(sdkVersion);
    default:
      throw new Error(`Platform '${platform}' is not supported.`);
  }
}

export default (program: Command) => {
  program
    .command('remove-sdk-version')
    .alias('remove-sdk', 'rm-sdk', 'rs')
    .description('Removes SDK version.')
    .usage(`
    
To remove versioned code for the oldest supported SDK on iOS, run:
${chalk.gray('>')} ${chalk.italic.cyan('et remove-sdk-version --platform ios')}`
    )
    .option('-p, --platform <string>', `Specifies a platform for which the SDK code should be removed. Supported platforms: ${SUPPORTED_PLATFORMS.map(platform => chalk.cyan(platform)).join(', ')}.`)
    .option('-s, --sdkVersion [string]', 'SDK version to remove. Defaults to the oldest supported SDK version.')
    .asyncAction(action);
};
