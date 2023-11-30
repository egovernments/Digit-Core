import React from "react";
import { SportsMotorsports } from "./SportsMotorsports";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsMotorsports",
  component: SportsMotorsports,
};

export const Default = () => <SportsMotorsports />;
export const Fill = () => <SportsMotorsports fill="blue" />;
export const Size = () => <SportsMotorsports height="50" width="50" />;
export const CustomStyle = () => <SportsMotorsports style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsMotorsports className="custom-class" />;

export const Clickable = () => <SportsMotorsports onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsMotorsports {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
