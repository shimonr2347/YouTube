import React from 'react';
import './ListMenu.css';
import { Link,useNavigate } from 'react-router-dom';



function ListMenu() {
  const Navigate = useNavigate();
  const userId = sessionStorage.getItem('userId');

  return (
    <li className="list-group-item">
      <ul className="nav flex-column">
        <li className="nav-item">
          <a
            href="#"
            className="nav-link active d-flex align-items-center text-dark"
            aria-current="page"
          >
            <i className="bi bi-house-door-fill "></i>
            <span>Home</span>
          </a>
        </li>
        <li className="nav-item">
          <a
            href="#"
            className="nav-link active d-flex align-items-center text-dark"
            aria-current="page"
          >
            <i className="bi bi-search"></i>
            <span>Explore</span>
          </a>
        </li>
        <li className="nav-item">
          <a
            href="#"
            className="nav-link active d-flex align-items-center text-dark"
            aria-current="page"
          >
            <i className="bi bi-camera-video-fill"></i>
            <span>Shorts</span>
          </a>
        </li>
        <li className="nav-item">
          <a
            href="#"
            className="nav-link active d-flex align-items-center text-dark"
            aria-current="page"
          >
            <i className="bi bi-collection-play"></i>
            <span>Subscriptions</span>
          </a>
        </li>
        <li className="nav-item">
          {userId && (
            <Link to={`/users/${userId}`}
              className="nav-link active d-flex align-items-center text-dark"
              aria-current="page"
            >
              <i className="bi bi-person-circle"></i>
              <span>My Page</span>
            </Link>
          )}

        </li>
      </ul>
    </li>
  );
}

export default ListMenu;