import React from "react";
import { LocationOn } from "./LocationOn";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocationOn",
  component: LocationOn,
};

export const Default = () => <LocationOn />;
export const Fill = () => <LocationOn fill="blue" />;
export const Size = () => <LocationOn height="50" width="50" />;
export const CustomStyle = () => <LocationOn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocationOn className="custom-class" />;

export const Clickable = () => <LocationOn onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocationOn {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
