import React from "react";
import { SportsTennis } from "./SportsTennis";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsTennis",
  component: SportsTennis,
};

export const Default = () => <SportsTennis />;
export const Fill = () => <SportsTennis fill="blue" />;
export const Size = () => <SportsTennis height="50" width="50" />;
export const CustomStyle = () => <SportsTennis style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsTennis className="custom-class" />;

export const Clickable = () => <SportsTennis onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsTennis {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
