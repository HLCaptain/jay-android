import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';

import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero', styles.heroBanner)}>
      <div className={styles.heroContent + " row"}>
        <img src="img/logo.svg" role="img" />
        <div className={styles.heroText + " col"}>
          <h1 className="hero__title">{siteConfig.title}</h1>
          <p className="hero__subtitle">{siteConfig.tagline}</p>
          <div className={styles.buttons + " --ifm-spacing-horizontal"}>
            <Link
              className="button button--primary button--lg"
              to="/docs/intro">
              Get Started with Jay 🐦
            </Link>
            <Link
              className="button button--outline button--primary button--lg"
              href="https://ko-fi.com/illyan">
              Support Jay 💖
            </Link>
          </div>
        </div>
      </div>
    </header>
  );
}

export default function Home(): JSX.Element {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`Hello from ${siteConfig.title}`}
      description="Jay is a Data Analytics Solution. This page provides documentation and general information on the Android app client for Jay.">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
