package illyan.jay.ui.privacy_policy

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import illyan.jay.R
import illyan.jay.ui.components.JayDialogContent
import illyan.jay.ui.components.JayTextCard
import illyan.jay.ui.components.PreviewLightDarkTheme
import illyan.jay.ui.profile.ProfileNavGraph
import illyan.jay.ui.theme.JayTheme

@ProfileNavGraph
@Destination
@Composable
fun PrivacyPolicy() {
    PrivacyPolicyDialogContent()
}

@Composable
fun PrivacyPolicyDialogContent(
    modifier: Modifier = Modifier,
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    JayDialogContent(
        modifier = modifier,
        textModifier = Modifier.heightIn(max = (screenHeightDp * 0.5f).dp),
        title = { PrivacyPolicyTitle() },
        text = { PrivacyPolicyScreen() },
        textPaddingValues = PaddingValues(),
    )
}

@Composable
fun PrivacyPolicyTitle() {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = stringResource(R.string.app_name))
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "",
            )
            Text(text = stringResource(R.string.privacy_policy))
        }
    }
}

@Composable
fun PrivacyPolicyScreen() {
    val privacyPolicy = "Privacy Policy\n" +
            "Balázs Püspök-Kiss built the Jay app as an Ad Supported app. This SERVICE is provided by Balázs Püspök-Kiss at no cost and is intended for use as is.\n" +
            "\n" +
            "This page is used to inform visitors regarding my policies with the collection, use, and disclosure of Personal Information if anyone decided to use my Service.\n" +
            "\n" +
            "If you choose to use my Service, then you agree to the collection and use of information in relation to this policy. The Personal Information that I collect is used for providing and improving the Service. I will not use or share your information with anyone except as described in this Privacy Policy.\n" +
            "\n" +
            "The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which are accessible at Jay unless otherwise defined in this Privacy Policy.\n" +
            "\n" +
            "Information Collection and Use\n" +
            "\n" +
            "For a better experience, while using our Service, I may require you to provide us with certain personally identifiable information. The information that I request will be retained on your device and is not collected by me in any way.\n" +
            "\n" +
            "The app does use third-party services that may collect information used to identify you.\n" +
            "\n" +
            "Link to the privacy policy of third-party service providers used by the app\n" +
            "\n" +
            "Google Play Services\n" +
            "AdMob\n" +
            "Google Analytics for Firebase\n" +
            "Firebase Crashlytics\n" +
            "Mapbox\n" +
            "Log Data\n" +
            "\n" +
            "I want to inform you that whenever you use my Service, in a case of an error in the app I collect data and information (through third-party products) on your phone called Log Data. This Log Data may include information such as your device Internet Protocol (“IP”) address, device name, operating system version, the configuration of the app when utilizing my Service, the time and date of your use of the Service, and other statistics.\n" +
            "\n" +
            "Cookies\n" +
            "\n" +
            "Cookies are files with a small amount of data that are commonly used as anonymous unique identifiers. These are sent to your browser from the websites that you visit and are stored on your device's internal memory.\n" +
            "\n" +
            "This Service does not use these “cookies” explicitly. However, the app may use third-party code and libraries that use “cookies” to collect information and improve their services. You have the option to either accept or refuse these cookies and know when a cookie is being sent to your device. If you choose to refuse our cookies, you may not be able to use some portions of this Service.\n" +
            "\n" +
            "Service Providers\n" +
            "\n" +
            "I may employ third-party companies and individuals due to the following reasons:\n" +
            "\n" +
            "To facilitate our Service;\n" +
            "To provide the Service on our behalf;\n" +
            "To perform Service-related services; or\n" +
            "To assist us in analyzing how our Service is used.\n" +
            "I want to inform users of this Service that these third parties have access to their Personal Information. The reason is to perform the tasks assigned to them on our behalf. However, they are obligated not to disclose or use the information for any other purpose.\n" +
            "\n" +
            "Security\n" +
            "\n" +
            "I value your trust in providing us your Personal Information, thus we are striving to use commercially acceptable means of protecting it. But remember that no method of transmission over the internet, or method of electronic storage is 100% secure and reliable, and I cannot guarantee its absolute security.\n" +
            "\n" +
            "Links to Other Sites\n" +
            "\n" +
            "This Service may contain links to other sites. If you click on a third-party link, you will be directed to that site. Note that these external sites are not operated by me. Therefore, I strongly advise you to review the Privacy Policy of these websites. I have no control over and assume no responsibility for the content, privacy policies, or practices of any third-party sites or services.\n" +
            "\n" +
            "Children’s Privacy\n" +
            "\n" +
            "These Services do not address anyone under the age of 13. I do not knowingly collect personally identifiable information from children under 13 years of age. In the case I discover that a child under 13 has provided me with personal information, I immediately delete this from our servers. If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact me so that I will be able to do the necessary actions.\n" +
            "\n" +
            "Changes to This Privacy Policy\n" +
            "\n" +
            "I may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes. I will notify you of any changes by posting the new Privacy Policy on this page.\n" +
            "\n" +
            "This policy is effective as of 2023-03-06\n" +
            "\n" +
            "Contact Us\n" +
            "\n" +
            "If you have any questions or suggestions about my Privacy Policy, do not hesitate to contact me at pkblazsak@gmail.com.\n" +
            "\n" +
            "This privacy policy page was created at privacypolicytemplate.net and modified/generated by App Privacy Policy Generator"
    JayTextCard {
        Text(text = privacyPolicy)
    }
}

@PreviewLightDarkTheme
@Composable
private fun PrivacyPolicyDialogContentPreview() {
    JayTheme {
        JayDialogContent(
            modifier = Modifier.width(300.dp)
        ) {
            PrivacyPolicyDialogContent()
        }
    }
}
