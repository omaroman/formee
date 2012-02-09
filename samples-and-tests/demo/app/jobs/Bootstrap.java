/**
 * Author: OMAROMAN
 * Date: 12/1/11
 * Time: 10:28 AM
 */

package jobs;

import models.Author;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {

    @Override
    public void doJob() {

//        Fixtures.deleteAllModels();
        if (Author.count() == 0) {
            Fixtures.loadModels("initial-data.yml");
            Logger.info("Loaded initial data");
        }

    }
}
