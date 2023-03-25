import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Analyze your Data',
    Image: require('@site/static/img/default_session.png').default,
    description: (
      <>
        Jay was designed to engage with everyday people, making data analytics
        easier to grasp. Understand your habits from Start to Finish.
      </>
    ),
  },
  {
    title: 'Record Sessions',
    Image: require('@site/static/img/sessions.png').default,
    description: (
      <>
        Whether you're driving to work or do your friend a solid, you can record
        data on-route and get additional information about it. You can also sync your
        Sessions onto the cloud.
      </>
    ),
  },
  {
    title: 'Multi-user Support',
    Image: require('@site/static/img/settings_2.png').default,
    description: (
      <>
        Jay supports multiple users on a single device to make data analytics
        more accessible for companies as well, e.g. a single device can monitor
        multiple bus driver's performance. Not constrained by a single device,
        the user can have its settings dialed in and ready to go when hopping
        on a new vehicle.
      </>
    ),
  },
  {
    title: 'Offline First Experience',
    Image: require('@site/static/img/func_2.png').default,
    description: (
      <>
        While Jay offers several functionalities to make data analytics more accessible
        to users, it is built on a fully offline basis. Jay's core functionality is and
        will be available when the device is offline and never require you to upload
        or share your data ever. Data analytics is fully offline from collection to stats.
      </>
    ),
  },
];

function Feature({Image, title, description}) {
  return (
    <div className={clsx('row padding--md')}>
      <div className="col col--7">
        <img className={styles.featureImage} role="img" src={Image}  />
      </div>
      <div className="col col--5 padding--lg ">
        <div className={styles.featureText}>
          <h2>{title}</h2>
          <p>{description}</p>
        </div>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="rows">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
