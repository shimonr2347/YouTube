import React from 'react';
import ListMenu from './ListMenu';
import { Link } from 'react-router-dom';

function Menu({ isOffcanvasOpen, resetSearch, toggleOffcanvas }) {
  return (
    <div className={`offcanvas offcanvas-start ${isOffcanvasOpen ? 'show' : ''}`} tabIndex="-1" id="offcanvas" aria-labelledby="offcanvasLabel" style={{ width: '250px' }}>
      <div className="offcanvas-header">
        <div className="col d-flex justify-content-start ">
          <button className="btn menu-expand-btn" onClick={toggleOffcanvas}>
            <i className="bi bi-list"></i>
          </button>
          <Link className="utbLogo btn btn-action m-0"  >
            <i className="bi bi-youtube"></i>
          </Link>
        </div>
        <button type="button" className="btn-close" onClick={toggleOffcanvas} aria-label="Close"></button>
      </div>
      <div className="offcanvas-body">
        <ListMenu />
      </div>
    </div>
  );
}

export default Menu;