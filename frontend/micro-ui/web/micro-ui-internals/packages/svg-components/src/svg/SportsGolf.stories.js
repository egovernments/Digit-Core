import React from "react";
import { SportsGolf } from "./SportsGolf";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsGolf",
  component: SportsGolf,
};

export const Default = () => <SportsGolf />;
export const Fill = () => <SportsGolf fill="blue" />;
export const Size = () => <SportsGolf height="50" width="50" />;
export const CustomStyle = () => <SportsGolf style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsGolf className="custom-class" />;

export const Clickable = () => <SportsGolf onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsGolf {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
