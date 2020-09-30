/**
 * Copyright © 2014 Instituto Superior Técnico
 *
 * This file is part of MGP Viewer.
 *
 * FenixEdu Spaces is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Spaces is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Spaces.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.bennu;

import org.fenixedu.bennu.spring.BennuSpringModule;
import org.fenixedu.commons.configuration.ConfigurationInvocationHandler;
import org.fenixedu.commons.configuration.ConfigurationManager;
import org.fenixedu.commons.configuration.ConfigurationProperty;

@BennuSpringModule(basePackages = "pt.ist.applications.admissions.ui", bundles = "ApplicationsAdmissionsResources")
public class ApplicationsAdmissionsConfiguration {

    @ConfigurationManager(description = "Applications Admissions Configuration")
    public interface ConfigurationProperties {
        @ConfigurationProperty(key = "drive.contest.dir")
        public String contestDir();
        
        @ConfigurationProperty(key = "drive.client.app.user")
        public String contestAppUser();
        
        @ConfigurationProperty(key = "recaptcha.site.key")
        public String recaptchaSiteKey();
        
        @ConfigurationProperty(key = "recaptcha.secret.key")
        public String recaptchaSecretKey();
        
        @ConfigurationProperty(key = "candidacy.basepath", defaultValue = "/applications-admissions/#/candidate/")
        public String candidacyBasePath();

        @ConfigurationProperty(key = "admissions.email.to")
        public String emailTo();
      
    }

    public static ConfigurationProperties getConfiguration() {
        return ConfigurationInvocationHandler.getConfiguration(ConfigurationProperties.class);
    }

}
