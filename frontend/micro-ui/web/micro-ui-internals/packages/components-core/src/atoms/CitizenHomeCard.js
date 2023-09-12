import React from "react";
import { Link } from "react-router-dom";

const CitizenHomeCard = ({ header, links = [], state, Icon, Info, isInfo = false, styles }) => {
  return (
    <div className="digit-citizen-home-card" style={styles ? styles : {}}>
      <div className="digit-header">
        <h2>{header}</h2>
        <Icon />
      </div>

      <div className="digit-links">
        {links.map((e, i) => (
          <div className="digit-links-wrapper">
            <Link key={i} to={{ pathname: e.link, state: e.state }}>
              {e.i18nKey}
            </Link>
          </div>
        ))}
      </div>
      <div>{isInfo ? <Info /> : null}</div>
    </div>
  );
};

export default CitizenHomeCard;
